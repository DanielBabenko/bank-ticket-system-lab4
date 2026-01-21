/*package com.example.productservice.integration;

import com.example.productservice.ProductServiceApplication;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.dto.ProductRequest;
import com.example.productservice.model.entity.Product;
import com.example.productservice.model.enums.AssignmentRole;
import com.example.productservice.repository.ProductRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderResult;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ProductServiceApplication.class)
public class ProductIntegrationTest {

    private static final String SECRET = "test-secret-very-long-string-at-least-32-bytes-123456";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

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
        registry.add("resilience4j.circuitbreaker.instances.assignment-service.registerHealthIndicator", () -> "false");

        // JWT secret for security in application
        registry.add("jwt.secret", () -> SECRET);

        // Kafka properties - отключаем Kafka для тестов, будем мокать
        registry.add("spring.kafka.topics.product-deleted", () -> "product.deleted");
        registry.add("spring.autoconfigure.exclude", () ->
                "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private com.example.productservice.feign.AssignmentServiceClient assignmentServiceClient;

    @MockitoBean
    private KafkaSender<String, String> kafkaSender;

    @MockitoBean
    private SenderResult<String> senderResult;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        // Настраиваем мок KafkaSender для успешной отправки
        // send() возвращает Flux<SenderResult<String>>, поэтому используем Flux.empty()
        when(kafkaSender.send(any(reactor.core.publisher.Mono.class))).thenReturn(Flux.empty());
    }

    // Helper — generate JWT token with uid and role claims
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
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        String token = generateToken(uid, role);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void createProduct_withAdminRole_shouldReturnCreated() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setDescription("Test Description");

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_ADMIN");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductDto> response = restTemplate.postForEntity(
                "/api/v1/products",
                entity,
                ProductDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getName());
        assertEquals("Test Description", response.getBody().getDescription());
        assertNotNull(response.getBody().getId());

        // Verify in database
        Optional<Product> savedProduct = productRepository.findById(response.getBody().getId());
        assertTrue(savedProduct.isPresent());
        assertEquals("Test Product", savedProduct.get().getName());
    }

    @Test
    void createProduct_withDuplicateName_shouldReturnConflict() {
        Product existing = new Product();
        UUID productId = UUID.randomUUID();
        existing.setId(productId);
        existing.setName("Existing Product");
        existing.setDescription("Existing Description");
        productRepository.save(existing);

        ProductRequest request = new ProductRequest();
        request.setName("Existing Product");
        request.setDescription("New Description");

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_ADMIN");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductDto> response = restTemplate.postForEntity(
                "/api/v1/products",
                entity,
                ProductDto.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void createProduct_withClientRole_shouldReturnForbidden() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setDescription("Test Description");

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_CLIENT");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductDto> response = restTemplate.postForEntity(
                "/api/v1/products",
                entity,
                ProductDto.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getProduct_shouldReturnProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Test Description");
        productRepository.save(product);

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_CLIENT");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                "/api/v1/products/" + product.getId(),
                HttpMethod.GET,
                entity,
                ProductDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(product.getId(), response.getBody().getId());
        assertEquals("Test Product", response.getBody().getName());
        assertEquals("Test Description", response.getBody().getDescription());
    }

    @Test
    void getProduct_notFound_shouldReturnNotFound() {
        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_CLIENT");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                "/api/v1/products/" + UUID.randomUUID(),
                HttpMethod.GET,
                entity,
                ProductDto.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listProducts_withPagination_shouldReturnPage() {
        for (int i = 0; i < 5; i++) {
            Product product = new Product();
            product.setId(UUID.randomUUID());
            product.setName("Product " + i);
            product.setDescription("Description " + i);
            productRepository.save(product);
        }

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_CLIENT");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProductDto[]> response = restTemplate.exchange(
                "/api/v1/products?page=0&size=3",
                HttpMethod.GET,
                entity,
                ProductDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().length);
    }

    @Test
    void listProducts_pageSizeTooLarge_shouldReturnBadRequest() {
        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_CLIENT");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/products?page=0&size=100",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.toLowerCase().contains("size") || body.toLowerCase().contains("bad"),
                "Expected error description about size in response body, actual: " + body);
    }

    @Test
    void updateProduct_asAdmin_shouldUpdateProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Original Name");
        product.setDescription("Original Description");
        productRepository.save(product);

        UUID actorId = UUID.randomUUID();
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");

        // Mock assignment service - user is not owner but admin role will allow update
        when(assignmentServiceClient.existsByUserAndProductAndRole(
                eq(actorId), eq(product.getId()), eq(AssignmentRole.PRODUCT_OWNER.name())))
                .thenReturn(false);

        HttpHeaders headers = headersWithToken(actorId, "ROLE_ADMIN");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.PUT,
                entity,
                ProductDto.class,
                product.getId()
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Product", response.getBody().getName());
        assertEquals("Updated Description", response.getBody().getDescription());

        // Verify update in database
        Optional<Product> updatedProduct = productRepository.findById(product.getId());
        assertTrue(updatedProduct.isPresent());
        assertEquals("Updated Product", updatedProduct.get().getName());
        assertEquals("Updated Description", updatedProduct.get().getDescription());
    }

    @Test
    void updateProduct_asProductOwner_shouldUpdateProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Original Name");
        product.setDescription("Original Description");
        productRepository.save(product);

        UUID actorId = UUID.randomUUID();
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");

        // Mock assignment service - user is product owner
        when(assignmentServiceClient.existsByUserAndProductAndRole(
                eq(actorId), eq(product.getId()), eq(AssignmentRole.PRODUCT_OWNER.name())))
                .thenReturn(true);

        HttpHeaders headers = headersWithToken(actorId, "ROLE_CLIENT");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.PUT,
                entity,
                ProductDto.class,
                product.getId()
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Product", response.getBody().getName());
        assertEquals("Original Description", response.getBody().getDescription());
    }

    @Test
    void updateProduct_withoutAuthorization_shouldReturnUnauthorized() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Product");
        product.setDescription("Description");
        productRepository.save(product);

        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductRequest> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.PUT,
                entity,
                ProductDto.class,
                product.getId()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updateProduct_withoutPermission_shouldReturnForbidden() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Product");
        product.setDescription("Description");
        productRepository.save(product);

        UUID actorId = UUID.randomUUID();
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");

        // Mock assignment service - user is not owner and not admin
        when(assignmentServiceClient.existsByUserAndProductAndRole(
                eq(actorId), eq(product.getId()), eq(AssignmentRole.PRODUCT_OWNER.name())))
                .thenReturn(false);

        HttpHeaders headers = headersWithToken(actorId, "ROLE_CLIENT");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.PUT,
                entity,
                ProductDto.class,
                product.getId()
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteProduct_asAdmin_shouldDeleteAndSendKafkaMessage() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Product to delete");
        product.setDescription("Description");
        productRepository.save(product);

        UUID actorId = UUID.randomUUID();

        // Mock assignment service - user is not owner but admin role will allow delete
        when(assignmentServiceClient.existsByUserAndProductAndRole(
                eq(actorId), eq(product.getId()), eq(AssignmentRole.PRODUCT_OWNER.name())))
                .thenReturn(false);

        HttpHeaders headers = headersWithToken(actorId, "ROLE_ADMIN");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                product.getId()
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify product is deleted from database
        Optional<Product> deletedProduct = productRepository.findById(product.getId());
        assertFalse(deletedProduct.isPresent());

        // Verify Kafka message was sent
        verify(kafkaSender, times(1)).send(any(reactor.core.publisher.Mono.class));
    }

    @Test
    void deleteProduct_asProductOwner_shouldDeleteAndSendKafkaMessage() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Product to delete");
        product.setDescription("Description");
        productRepository.save(product);

        UUID actorId = UUID.randomUUID();

        // Mock assignment service - user is product owner
        when(assignmentServiceClient.existsByUserAndProductAndRole(
                eq(actorId), eq(product.getId()), eq(AssignmentRole.PRODUCT_OWNER.name())))
                .thenReturn(true);

        HttpHeaders headers = headersWithToken(actorId, "ROLE_CLIENT");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                product.getId()
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify product is deleted from database
        Optional<Product> deletedProduct = productRepository.findById(product.getId());
        assertFalse(deletedProduct.isPresent());

        // Verify Kafka message was sent
        verify(kafkaSender, times(1)).send(any(reactor.core.publisher.Mono.class));
    }

    @Test
    void deleteProduct_withoutAuthorization_shouldReturnUnauthorized() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Product");
        product.setDescription("Description");
        productRepository.save(product);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                product.getId()
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteProduct_withoutPermission_shouldReturnForbidden() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Product");
        product.setDescription("Description");
        productRepository.save(product);

        UUID actorId = UUID.randomUUID();

        // Mock assignment service - user is not owner and not admin
        when(assignmentServiceClient.existsByUserAndProductAndRole(
                eq(actorId), eq(product.getId()), eq(AssignmentRole.PRODUCT_OWNER.name())))
                .thenReturn(false);

        HttpHeaders headers = headersWithToken(actorId, "ROLE_CLIENT");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                product.getId()
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Verify product is NOT deleted
        Optional<Product> existingProduct = productRepository.findById(product.getId());
        assertTrue(existingProduct.isPresent());

        // Verify Kafka message was NOT sent
        verify(kafkaSender, never()).send(any(reactor.core.publisher.Mono.class));
    }

    @Test
    void productExists_endpoint_shouldReturnBoolean() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Existing Product");
        product.setDescription("Description");
        productRepository.save(product);

        UUID nonExistingId = UUID.randomUUID();

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_CLIENT");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Boolean> response1 = restTemplate.exchange(
                "/api/v1/products/{id}/exists",
                HttpMethod.GET,
                entity,
                Boolean.class,
                product.getId()
        );

        ResponseEntity<Boolean> response2 = restTemplate.exchange(
                "/api/v1/products/{id}/exists",
                HttpMethod.GET,
                entity,
                Boolean.class,
                nonExistingId
        );

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertTrue(response1.getBody());

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertFalse(response2.getBody());
    }

    @Test
    void createProduct_withEmptyName_shouldReturnBadRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("");
        request.setDescription("Description");

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_ADMIN");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductDto> response = restTemplate.postForEntity(
                "/api/v1/products",
                entity,
                ProductDto.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createProduct_withEmptyDescription_shouldReturnBadRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Valid Name");
        request.setDescription("");

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_ADMIN");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductDto> response = restTemplate.postForEntity(
                "/api/v1/products",
                entity,
                ProductDto.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createProduct_withWhitespaceOnly_shouldReturnBadRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("   ");
        request.setDescription("   ");

        HttpHeaders headers = headersWithToken(UUID.randomUUID(), "ROLE_ADMIN");
        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductDto> response = restTemplate.postForEntity(
                "/api/v1/products",
                entity,
                ProductDto.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteProduct_withKafkaError_stillDeletesProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Product to delete");
        product.setDescription("Description");
        productRepository.save(product);

        UUID actorId = UUID.randomUUID();

        // Mock assignment service - admin role will allow delete
        when(assignmentServiceClient.existsByUserAndProductAndRole(
                eq(actorId), eq(product.getId()), eq(AssignmentRole.PRODUCT_OWNER.name())))
                .thenReturn(false);

        // Mock KafkaSender to simulate an error
        when(kafkaSender.send(any(reactor.core.publisher.Mono.class)))
                .thenReturn(Flux.error(new RuntimeException("Kafka error")));

        HttpHeaders headers = headersWithToken(actorId, "ROLE_ADMIN");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                product.getId()
        );

        // Should still delete the product even if Kafka fails
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Optional<Product> deletedProduct = productRepository.findById(product.getId());
        assertTrue(deletedProduct.isPresent());

        verify(kafkaSender, times(1)).send(any(reactor.core.publisher.Mono.class));
    }
}*/