/*package com.example.fileservice.integration;

import com.example.fileservice.FileServiceApplication;
import com.example.fileservice.application.dto.FileDto;
import com.example.fileservice.adapters.outbound.feign.ApplicationServiceClientFeign;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
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
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FileRepositoryPort fileRepository;

    @Autowired
    private StoragePort minioService;

    @Autowired
    private MinioClient minioClient;

    @MockitoBean
    private ApplicationServiceClientFeign applicationServiceClient;

    @BeforeEach
    void setUp() throws Exception {
        fileRepository.deleteAll();
        minioService.initializeBucket();

        // Mock Feign client to return empty applications
        when(applicationServiceClient.getApplicationsByFile(any(UUID.class)))
                .thenReturn(Collections.emptyList());

        // Enable PATCH support in RestTemplate if needed
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    // JWT helper
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

    private Resource createTestFileResource(String content, String filename) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    @Test
    void uploadFile_shouldReturnCreated() {
        UUID userId = UUID.randomUUID();
        HttpHeaders headers = headersWithToken(userId, "ROLE_CLIENT");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("test content", "test.txt"));
        body.add("description", "Test description");

        ResponseEntity<FileDto> response = restTemplate.postForEntity(
                "/api/v1/files/upload",
                new HttpEntity<>(body, headers),
                FileDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        FileDto fileDto = response.getBody();
        assertNotNull(fileDto);
        assertEquals("test.txt", fileDto.getOriginalName());
        assertEquals(userId, fileDto.getUploaderId());
        assertTrue(fileRepository.existsById(fileDto.getId()));
        assertTrue(minioService.fileExists(fileDto.getId() + ".txt"));
    }

    @Test
    void downloadFile_asUploader_shouldReturnOk() {
        // Upload first
        UUID userId = UUID.randomUUID();
        HttpHeaders uploadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("content", "file.txt"));

        FileDto uploaded = restTemplate.postForEntity(
                "/api/v1/files/upload",
                new HttpEntity<>(body, uploadHeaders),
                FileDto.class
        ).getBody();

        HttpHeaders downloadHeaders = headersWithToken(userId, "ROLE_CLIENT");
        ResponseEntity<Resource> response = restTemplate.exchange(
                "/api/v1/files/{id}/download",
                HttpMethod.GET,
                new HttpEntity<>(downloadHeaders),
                Resource.class,
                uploaded.getId()
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("file.txt", response.getHeaders().getContentDisposition().getFilename());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
    }

    @Test
    void deleteFile_asUploader_shouldReturnNoContent() {
        UUID userId = UUID.randomUUID();
        HttpHeaders headers = headersWithToken(userId, "ROLE_CLIENT");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createTestFileResource("delete content", "delete.txt"));

        FileDto uploaded = restTemplate.postForEntity(
                "/api/v1/files/upload",
                new HttpEntity<>(body, headers),
                FileDto.class
        ).getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/files/{id}",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class,
                uploaded.getId()
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(fileRepository.existsById(uploaded.getId()));
        assertFalse(minioService.fileExists(uploaded.getId() + ".txt"));
    }

    @Test
    void listFiles_shouldReturnList() {
        UUID userId = UUID.randomUUID();
        HttpHeaders headers = headersWithToken(userId, "ROLE_CLIENT");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Upload two files
        restTemplate.postForEntity("/api/v1/files/upload",
                new HttpEntity<>(new LinkedMultiValueMap<>() {{
                    add("file", createTestFileResource("1", "f1.txt"));
                }}, headers), FileDto.class);

        restTemplate.postForEntity("/api/v1/files/upload",
                new HttpEntity<>(new LinkedMultiValueMap<>() {{
                    add("file", createTestFileResource("2", "f2.txt"));
                }}, headers), FileDto.class);

        ResponseEntity<FileDto[]> response = restTemplate.exchange(
                "/api/v1/files?page=0&size=10",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FileDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).length);
    }
}
*/