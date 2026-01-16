package com.example.fileservice.integration;

import com.example.fileservice.FileServiceApplication;
import com.example.fileservice.dto.ApplicationInfoDto;
import com.example.fileservice.dto.FileDto;
import com.example.fileservice.feign.ApplicationServiceClient;
import com.example.fileservice.model.entity.File;
import com.example.fileservice.repository.FileRepository;
import com.example.fileservice.service.MinioService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = FileServiceApplication.class)
public class FileIntegrationTest {

    private static final String SECRET = "test-secret-very-long-string-at-least-32-bytes-123456";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> MINIO = new GenericContainer<>("minio/minio")
            .withCommand("server /data --console-address :9001")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withExposedPorts(9000, 9001);

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.cloud.discovery.enabled", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");

        // MinIO properties
        registry.add("minio.endpoint", () -> "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000));
        registry.add("minio.access-key", () -> "minioadmin");
        registry.add("minio.secret-key", () -> "minioadmin");
        registry.add("minio.secure", () -> "false");
        registry.add("minio.bucket.name", () -> "files");
        registry.add("minio.url-expiry-seconds", () -> "3600");

        // Kafka properties
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "file-service-group");
        registry.add("spring.kafka.topics.file-attach-request", () -> "file.attach.request");

        // JWT secret
        registry.add("jwt.secret", () -> SECRET);

        // Exclude Kafka auto-config if needed, but since we have listener, keep it
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private MinioClient minioClient;  // To verify storage directly if needed

    @MockitoBean
    private ApplicationServiceClient applicationServiceClient;

    @BeforeEach
    void setUp() throws Exception {
        fileRepository.deleteAll();

        // Initialize MinIO bucket
        minioService.initializeBucket();

        // Mock Feign client to return empty applications
        when(applicationServiceClient.getApplicationsByFile(any(UUID.class)))
                .thenReturn(Collections.emptyList());

        // Set up RestTemplate to support PATCH if needed (though not used here)
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    // Helper to generate JWT token
    private String generateToken(UUID uid, String role) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = Date.from(Instant.now());
        Date exp = Date.from(Instant.now().plusSeconds(3600));
        return Jwts.builder()
                .setSubject(uid.toString())
                .claim("uid", uid.toString())
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private HttpHeaders headersWithToken(UUID uid, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        String token = generateToken(uid, role);
        headers.setBearerAuth(token);
        return headers;
    }

    // Helper to create a test file resource
    private Resource createTestFileResource(String content, String filename) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    @Test
    void uploadFile_withValidUser_shouldReturnCreated() {
        UUID userId = UUID.randomUUID();
        HttpHeaders headers = headersWithToken(userId, "ROLE_CLIENT");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));
        body.add("description", "Test description");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<FileDto> response = restTemplate.postForEntity(
                "/api/v1/files/upload",
                entity,
                FileDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        FileDto fileDto = response.getBody();
        assertNotNull(fileDto);
        assertEquals("test.txt", fileDto.getOriginalName());
        assertEquals(userId, fileDto.getUploaderId());
        assertEquals("Test description", fileDto.getDescription());
        assertTrue(fileRepository.existsById(fileDto.getId()));
        assertTrue(minioService.fileExists(fileDto.getId() + ".txt"));
    }

    @Test
    void uploadFile_withEmptyFile_shouldReturnBadRequest() {
        UUID userId = UUID.randomUUID();
        HttpHeaders headers = headersWithToken(userId, "ROLE_CLIENT");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("", ""));  // Empty file and name

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<FileDto> response = restTemplate.postForEntity(
                "/api/v1/files/upload",
                entity,
                FileDto.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void downloadFile_asUploader_shouldReturnOk() throws IOException {
        // First upload a file
        UUID userId = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));

        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileDto> uploadResponse = restTemplate.postForEntity(
                "/api/v1/files/upload",
                uploadEntity,
                FileDto.class
        );

        UUID fileId = uploadResponse.getBody().getId();

        // Now download
        HttpHeaders downloadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        HttpEntity<Void> downloadEntity = new HttpEntity<>(downloadHeaders);

        ResponseEntity<Resource> response = restTemplate.exchange(
                "/api/v1/files/{id}/download",
                HttpMethod.GET,
                downloadEntity,
                Resource.class,
                fileId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test.txt", response.getHeaders().getContentDisposition().getFilename());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
    }

    @Test
    void downloadFile_asNonUploaderWithoutAdminOrManager_shouldReturnForbidden() {
        // Upload as user1
        UUID user1Id = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(user1Id, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));

        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileDto> uploadResponse = restTemplate.postForEntity(
                "/api/v1/files/upload",
                uploadEntity,
                FileDto.class
        );

        UUID fileId = uploadResponse.getBody().getId();

        // Download as user2 (client)
        UUID user2Id = UUID.randomUUID();
        HttpHeaders downloadHeaders = headersWithToken(user2Id, "ROLE_CLIENT");
        HttpEntity<Void> downloadEntity = new HttpEntity<>(downloadHeaders);

        ResponseEntity<Resource> response = restTemplate.exchange(
                "/api/v1/files/{id}/download",
                HttpMethod.GET,
                downloadEntity,
                Resource.class,
                fileId
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void downloadFile_asManager_shouldReturnOk() {
        // Upload as user
        UUID userId = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));

        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileDto> uploadResponse = restTemplate.postForEntity(
                "/api/v1/files/upload",
                uploadEntity,
                FileDto.class
        );

        UUID fileId = uploadResponse.getBody().getId();

        // Download as manager
        UUID managerId = UUID.randomUUID();
        HttpHeaders downloadHeaders = headersWithToken(managerId, "ROLE_MANAGER");
        HttpEntity<Void> downloadEntity = new HttpEntity<>(downloadHeaders);

        ResponseEntity<Resource> response = restTemplate.exchange(
                "/api/v1/files/{id}/download",
                HttpMethod.GET,
                downloadEntity,
                Resource.class,
                fileId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteFile_asUploader_shouldReturnNoContent() {
        // Upload file
        UUID userId = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));

        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileDto> uploadResponse = restTemplate.postForEntity(
                "/api/v1/files/upload",
                uploadEntity,
                FileDto.class
        );

        UUID fileId = uploadResponse.getBody().getId();
        String storageKey = fileId + ".txt";

        // Delete
        HttpHeaders deleteHeaders = headersWithToken(userId, "ROLE_CLIENT");
        HttpEntity<Void> deleteEntity = new HttpEntity<>(deleteHeaders);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/files/{id}",
                HttpMethod.DELETE,
                deleteEntity,
                Void.class,
                fileId
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(fileRepository.existsById(fileId));
        assertFalse(minioService.fileExists(storageKey));
    }

    @Test
    void deleteFile_asNonUploaderWithoutAdmin_shouldReturnForbidden() {
        // Upload as user1
        UUID user1Id = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(user1Id, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));

        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileDto> uploadResponse = restTemplate.postForEntity(
                "/api/v1/files/upload",
                uploadEntity,
                FileDto.class
        );

        UUID fileId = uploadResponse.getBody().getId();

        // Delete as user2 (client)
        UUID user2Id = UUID.randomUUID();
        HttpHeaders deleteHeaders = headersWithToken(user2Id, "ROLE_CLIENT");
        HttpEntity<Void> deleteEntity = new HttpEntity<>(deleteHeaders);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/files/{id}",
                HttpMethod.DELETE,
                deleteEntity,
                Void.class,
                fileId
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(fileRepository.existsById(fileId));
    }

    @Test
    void deleteFile_asAdmin_shouldReturnNoContent() {
        // Upload as user
        UUID userId = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));

        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileDto> uploadResponse = restTemplate.postForEntity(
                "/api/v1/files/upload",
                uploadEntity,
                FileDto.class
        );

        UUID fileId = uploadResponse.getBody().getId();
        String storageKey = fileId + ".txt";

        // Delete as admin
        UUID adminId = UUID.randomUUID();
        HttpHeaders deleteHeaders = headersWithToken(adminId, "ROLE_ADMIN");
        HttpEntity<Void> deleteEntity = new HttpEntity<>(deleteHeaders);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/files/{id}",
                HttpMethod.DELETE,
                deleteEntity,
                Void.class,
                fileId
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(fileRepository.existsById(fileId));
        assertFalse(minioService.fileExists(storageKey));
    }

    @Test
    void listFiles_shouldReturnList() {
        // Upload two files
        UUID userId = UUID.randomUUID();
        HttpHeaders headers = headersWithToken(userId, "ROLE_CLIENT");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body1 = new LinkedMultiValueMap<>();
        body1.add("file", createTestFileResource("content1", "file1.txt"));
        HttpEntity<MultiValueMap<String, Object>> entity1 = new HttpEntity<>(body1, headers);
        restTemplate.postForEntity("/api/v1/files/upload", entity1, FileDto.class);

        MultiValueMap<String, Object> body2 = new LinkedMultiValueMap<>();
        body2.add("file", createTestFileResource("content2", "file2.txt"));
        HttpEntity<MultiValueMap<String, Object>> entity2 = new HttpEntity<>(body2, headers);
        restTemplate.postForEntity("/api/v1/files/upload", entity2, FileDto.class);

        // List
        HttpHeaders listHeaders = headersWithToken(userId, "ROLE_CLIENT");
        HttpEntity<Void> listEntity = new HttpEntity<>(listHeaders);

        ResponseEntity<FileDto[]> response = restTemplate.exchange(
                "/api/v1/files?page=0&size=20",
                HttpMethod.GET,
                listEntity,
                FileDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        FileDto[] files = response.getBody();
        assertNotNull(files);
        assertEquals(2, files.length);
    }

    @Test
    void getFileById_shouldReturnFileDto() {
        // Upload file
        UUID userId = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));

        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileDto> uploadResponse = restTemplate.postForEntity(
                "/api/v1/files/upload",
                uploadEntity,
                FileDto.class
        );

        UUID fileId = uploadResponse.getBody().getId();

        // Get
        HttpHeaders getHeaders = headersWithToken(userId, "ROLE_CLIENT");
        HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders);

        ResponseEntity<FileDto> response = restTemplate.exchange(
                "/api/v1/files/{id}",
                HttpMethod.GET,
                getEntity,
                FileDto.class,
                fileId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        FileDto fileDto = response.getBody();
        assertNotNull(fileDto);
        assertEquals(fileId, fileDto.getId());
        assertEquals("test.txt", fileDto.getOriginalName());
    }

    @Test
    void getFileById_withNonExistingId_shouldReturnNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_CLIENT");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<FileDto> response = restTemplate.exchange(
                "/api/v1/files/{id}",
                HttpMethod.GET,
                entity,
                FileDto.class,
                nonExistingId
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void checkFilesBatch_withValidIds_shouldReturnIds() {
        // Upload file
        UUID userId = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));

        HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileDto> uploadResponse = restTemplate.postForEntity(
                "/api/v1/files/upload",
                uploadEntity,
                FileDto.class
        );

        UUID fileId = uploadResponse.getBody().getId();

        // Check batch (internal endpoint, no auth in test)
        List<UUID> requestBody = List.of(fileId, UUID.randomUUID());  // One valid, one invalid
        HttpHeaders checkHeaders = new HttpHeaders();
        checkHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<UUID>> checkEntity = new HttpEntity<>(requestBody, checkHeaders);

        ResponseEntity<UUID[]> response = restTemplate.postForEntity(
                "/api/v1/files/check",
                checkEntity,
                UUID[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UUID[] ids = response.getBody();
        assertNotNull(ids);
        assertEquals(1, ids.length);
        assertEquals(fileId, ids[0]);
    }
}