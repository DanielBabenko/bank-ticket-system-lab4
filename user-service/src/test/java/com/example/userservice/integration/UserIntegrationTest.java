//package com.example.userservice.integration;
//
//import com.example.userservice.UserServiceApplication;
//import com.example.userservice.application.dto.UserDto;
//import com.example.userservice.presentation.dto.UserRequest;
//import com.example.userservice.domain.model.entity.User;
//import com.example.userservice.domain.model.enums.UserRole;
//import com.example.userservice.domain.repository.UserRepository;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.r2dbc.core.DatabaseClient;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.kafka.sender.KafkaSender;
//import reactor.kafka.sender.SenderResult;
//
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.time.Instant;
//import java.util.Date;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@Testcontainers
//@ActiveProfiles("test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        classes = UserServiceApplication.class)
//public class UserIntegrationTest {
//
//    private static final String SECRET = "test-secret-very-long-string-at-least-32-bytes-123456";
//
//    @Container
//    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
//            .withDatabaseName("testdb")
//            .withUsername("test")
//            .withPassword("test");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.r2dbc.url", () ->
//                String.format("r2dbc:postgresql://%s:%d/%s",
//                        POSTGRES.getHost(),
//                        POSTGRES.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
//                        POSTGRES.getDatabaseName()));
//        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
//        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
//
//        registry.add("spring.r2dbc.pool.enabled", () -> "true");
//        registry.add("spring.r2dbc.pool.initial-size", () -> "5");
//        registry.add("spring.r2dbc.pool.max-size", () -> "10");
//        registry.add("spring.r2dbc.pool.max-idle-time", () -> "30m");
//
//        registry.add("spring.flyway.enabled", () -> "false");
//        registry.add("spring.liquibase.enabled", () -> "false");
//        registry.add("spring.cloud.discovery.enabled", () -> "false");
//        registry.add("spring.cloud.config.enabled", () -> "false");
//
//        registry.add("jwt.secret", () -> SECRET);
//        registry.add("jwt.expiration-ms", () -> 3600000);
//
//        // Disable Kafka for tests or use test properties
//        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
//        registry.add("spring.kafka.topics.user-deleted", () -> "user.deleted.test");
//
//        registry.add("logging.level.org.springframework.r2dbc", () -> "DEBUG");
//    }
//
//    @LocalServerPort
//    private int port;
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private DatabaseClient databaseClient;
//
//    @MockitoBean
//    private KafkaSender<String, String> kafkaSender;
//
//    private UUID adminId;
//    private UUID clientId;
//    private UUID managerId;
//    private final String adminUsername = "admin";
//    private final String clientUsername = "client";
//    private final String managerUsername = "manager";
//
//    @BeforeEach
//    void setUp() {
//        createTableIfNotExists();
//        userRepository.deleteAll().block();
//
//        // create test users
//        adminId = createTestUser(adminUsername, "admin@example.com", UserRole.ROLE_ADMIN);
//        clientId = createTestUser(clientUsername, "client@example.com", UserRole.ROLE_CLIENT);
//        managerId = createTestUser(managerUsername, "manager@example.com", UserRole.ROLE_MANAGER);
//
//        // Mock KafkaSender to avoid actual Kafka connection
//        mockKafkaSender();
//    }
//
//    private void mockKafkaSender() {
//        SenderResult<String> mockResult = mock(SenderResult.class);
//        when(mockResult.exception()).thenReturn(null);
//
//        when(kafkaSender.send(any(Mono.class)))
//                .thenReturn(Flux.just(mockResult));
//
//        when(kafkaSender.send(any(Flux.class)))
//                .thenReturn(Flux.just(mockResult));
//    }
//
//    private void createTableIfNotExists() {
//        String createTableSql = """
//            CREATE TABLE IF NOT EXISTS app_user (
//                id UUID PRIMARY KEY,
//                username VARCHAR(100) NOT NULL UNIQUE,
//                email VARCHAR(255) NOT NULL UNIQUE,
//                password_hash VARCHAR(255) NOT NULL,
//                role VARCHAR(50) NOT NULL DEFAULT 'ROLE_CLIENT',
//                created_at TIMESTAMP WITH TIME ZONE NOT NULL,
//                updated_at TIMESTAMP,
//                version BIGINT DEFAULT 0
//            );
//            """;
//        try {
//            databaseClient.sql(createTableSql)
//                    .fetch()
//                    .rowsUpdated()
//                    .block();
//        } catch (Exception e) {
//            System.err.println("Error creating table: " + e.getMessage());
//        }
//    }
//
//    private UUID createTestUser(String username, String email, UserRole role) {
//        User user = new User();
//        user.setId(UUID.randomUUID());
//        user.setUsername(username);
//        user.setEmail(email);
//        user.setPasswordHash("$2a$10$hashedpassword");
//        user.setRole(role);
//        user.setCreatedAt(Instant.now());
//
//        return userRepository.save(user)
//                .map(User::getId)
//                .block();
//    }
//
//    private String generateToken(UUID uid, String role) {
//        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
//        Date now = Date.from(Instant.now());
//        Date exp = Date.from(Instant.now().plusSeconds(3600));
//        return Jwts.builder()
//                .setSubject(uid.toString())
//                .claim("uid", uid.toString())
//                .claim("role", role)
//                .setIssuedAt(now)
//                .setExpiration(exp)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    @Test
//    void createUser_shouldReturnCreatedUser() {
//        UserRequest request = new UserRequest();
//        request.setUsername("newuser");
//        request.setEmail("newuser@example.com");
//        request.setPassword("password123");
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.post()
//                .uri("/api/v1/users")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody(UserDto.class)
//                .consumeWith(response -> {
//                    UserDto userDto = response.getResponseBody();
//                    assertThat(userDto).isNotNull();
//                    assertThat(userDto.getId()).isNotNull();
//                    assertThat(userDto.getUsername()).isEqualTo("newuser");
//                    assertThat(userDto.getEmail()).isEqualTo("newuser@example.com");
//                    assertThat(userDto.getRole()).isEqualTo(UserRole.ROLE_CLIENT);
//                    assertThat(userDto.getCreatedAt()).isNotNull();
//                });
//    }
//
//    @Test
//    void createUser_withExistingUsername_shouldReturnConflict() {
//        UserRequest request = new UserRequest();
//        request.setUsername(adminUsername);
//        request.setEmail("newemail@example.com");
//        request.setPassword("password123");
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.post()
//                .uri("/api/v1/users")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isEqualTo(409);
//    }
//
//    @Test
//    void createUser_withExistingEmail_shouldReturnConflict() {
//        UserRequest request = new UserRequest();
//        request.setUsername("newusername");
//        request.setEmail("admin@example.com");
//        request.setPassword("password123");
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.post()
//                .uri("/api/v1/users")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isEqualTo(409);
//    }
//
//    @Test
//    void createUser_withInvalidData_shouldReturnBadRequest() {
//        UserRequest request = new UserRequest();
//        request.setUsername(null);
//        request.setEmail(null);
//        request.setPassword(null);
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.post()
//                .uri("/api/v1/users")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void getAllUsers_withPagination_shouldReturnPage() {
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.get()
//                .uri("/api/v1/users?page=0&size=2")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(UserDto.class)
//                .hasSize(2);
//    }
//
//    @Test
//    void getUserById_shouldReturnUser() {
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.get()
//                .uri("/api/v1/users/{id}", adminId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserDto.class)
//                .consumeWith(response -> {
//                    UserDto userDto = response.getResponseBody();
//                    assertThat(userDto).isNotNull();
//                    assertThat(userDto.getId()).isEqualTo(adminId);
//                    assertThat(userDto.getUsername()).isEqualTo(adminUsername);
//                });
//    }
//
//    @Test
//    void getUserById_notFound_shouldReturnNotFound() {
//        UUID nonExistingId = UUID.randomUUID();
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.get()
//                .uri("/api/v1/users/{id}", nonExistingId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void updateUser_asAdmin_shouldUpdateSuccessfully() {
//        UserRequest request = new UserRequest();
//        request.setUsername("updatedclient");
//        request.setEmail("updatedclient@example.com");
//        request.setPassword("newpassword123");
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}", clientId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserDto.class)
//                .consumeWith(response -> {
//                    UserDto userDto = response.getResponseBody();
//                    assertThat(userDto).isNotNull();
//                    assertThat(userDto.getId()).isEqualTo(clientId);
//                    assertThat(userDto.getUsername()).isEqualTo("updatedclient");
//                    assertThat(userDto.getEmail()).isEqualTo("updatedclient@example.com");
//                });
//    }
//
//    @Test
//    void updateUser_withoutAdminRights_shouldReturnForbidden() {
//        UserRequest request = new UserRequest();
//        request.setUsername("updatedclient");
//
//        String clientToken = generateToken(clientId, UserRole.ROLE_CLIENT.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}", managerId)
//                .headers(h -> h.setBearerAuth(clientToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void updateUser_notFound_shouldReturnNotFound() {
//        UUID nonExistingId = UUID.randomUUID();
//        UserRequest request = new UserRequest();
//        request.setUsername("updated");
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}", nonExistingId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void deleteUser_asAdmin_shouldDeleteSuccessfully() {
//        // create a user to delete
//        User userToDelete = new User();
//        userToDelete.setId(UUID.randomUUID());
//        userToDelete.setUsername("todelete");
//        userToDelete.setEmail("todelete@example.com");
//        userToDelete.setPasswordHash("$2a$10$hashed");
//        userToDelete.setRole(UserRole.ROLE_CLIENT);
//        userToDelete.setCreatedAt(Instant.now());
//
//        UUID userIdToDelete = userRepository.save(userToDelete)
//                .map(User::getId)
//                .block();
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.delete()
//                .uri("/api/v1/users/{id}", userIdToDelete)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isNoContent();
//
//        // Verify user is deleted
//        Boolean userExists = userRepository.existsById(userIdToDelete).block();
//        assertThat(userExists).isFalse();
//    }
//
//    @Test
//    void deleteUser_withoutAdminRights_shouldReturnForbidden() {
//        String clientToken = generateToken(clientId, UserRole.ROLE_CLIENT.name());
//
//        webTestClient.delete()
//                .uri("/api/v1/users/{id}", managerId)
//                .headers(h -> h.setBearerAuth(clientToken))
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void promoteToManager_asAdmin_shouldPromoteSuccessfully() {
//        User userBefore = userRepository.findById(clientId).block();
//        assertThat(userBefore.getRole()).isEqualTo(UserRole.ROLE_CLIENT);
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}/promote-manager", clientId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isNoContent();
//
//        User userAfter = userRepository.findById(clientId).block();
//        assertThat(userAfter.getRole()).isEqualTo(UserRole.ROLE_MANAGER);
//    }
//
//    @Test
//    void promoteToManager_alreadyManager_shouldDoNothing() {
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}/promote-manager", managerId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isNoContent();
//
//        User user = userRepository.findById(managerId).block();
//        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_MANAGER);
//    }
//
//    @Test
//    void promoteToManager_withoutAdminRights_shouldReturnForbidden() {
//        String clientToken = generateToken(clientId, UserRole.ROLE_CLIENT.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}/promote-manager", clientId)
//                .headers(h -> h.setBearerAuth(clientToken))
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void demoteToClient_asAdmin_shouldDemoteSuccessfully() {
//        User userBefore = userRepository.findById(managerId).block();
//        assertThat(userBefore.getRole()).isEqualTo(UserRole.ROLE_MANAGER);
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}/demote-manager", managerId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isNoContent();
//
//        User userAfter = userRepository.findById(managerId).block();
//        assertThat(userAfter.getRole()).isEqualTo(UserRole.ROLE_CLIENT);
//    }
//
//    @Test
//    void demoteToClient_withoutAdminRights_shouldReturnForbidden() {
//        String managerToken = generateToken(managerId, UserRole.ROLE_MANAGER.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}/demote-manager", managerId)
//                .headers(h -> h.setBearerAuth(managerToken))
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void userExists_existingUser_shouldReturnTrue() {
//        webTestClient.get()
//                .uri("/api/v1/users/{id}/exists", adminId)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class)
//                .isEqualTo(true);
//    }
//
//    @Test
//    void userExists_nonExistingUser_shouldReturnFalse() {
//        UUID nonExistingId = UUID.randomUUID();
//
//        webTestClient.get()
//                .uri("/api/v1/users/{id}/exists", nonExistingId)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void getUserRole_existingUser_shouldReturnRole() {
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.get()
//                .uri("/api/v1/users/{id}/role", adminId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserRole.class)
//                .isEqualTo(UserRole.ROLE_ADMIN);
//    }
//
//    @Test
//    void getUserRole_nonExistingUser_shouldReturnNotFound() {
//        UUID nonExistingId = UUID.randomUUID();
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.get()
//                .uri("/api/v1/users/{id}/role", nonExistingId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void createUser_withSpacesInUsername_shouldTrimSpaces() {
//        UserRequest request = new UserRequest();
//        request.setUsername("  testuser  ");
//        request.setEmail("testuser@example.com");
//        request.setPassword("password123");
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.post()
//                .uri("/api/v1/users")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody(UserDto.class)
//                .consumeWith(response -> {
//                    UserDto userDto = response.getResponseBody();
//                    assertThat(userDto).isNotNull();
//                    assertThat(userDto.getUsername()).isEqualTo("testuser");
//                });
//    }
//
//    @Test
//    void createUser_withSpacesAndCaseInEmail_shouldTrimAndLowercase() {
//        UserRequest request = new UserRequest();
//        request.setUsername("testuser");
//        request.setEmail("TestUser@Example.com");
//        request.setPassword("password123");
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.post()
//                .uri("/api/v1/users")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody(UserDto.class)
//                .consumeWith(response -> {
//                    UserDto userDto = response.getResponseBody();
//                    assertThat(userDto).isNotNull();
//                    assertThat(userDto.getEmail()).isEqualTo("testuser@example.com");
//                });
//    }
//
//    @Test
//    void updateUser_partialUpdate_shouldUpdateOnlyProvidedFields() {
//        User userBefore = userRepository.findById(clientId).block();
//        String originalEmail = userBefore.getEmail();
//
//        UserRequest request = new UserRequest();
//        request.setUsername("updatedusername");
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}", clientId)
//                .headers(h -> h.setBearerAuth(adminToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserDto.class)
//                .consumeWith(response -> {
//                    UserDto userDto = response.getResponseBody();
//                    assertThat(userDto).isNotNull();
//                    assertThat(userDto.getUsername()).isEqualTo("updatedusername");
//                    assertThat(userDto.getEmail()).isEqualTo(originalEmail);
//                });
//    }
//
//    @Test
//    void updateUser_bySelf_shouldReturnForbiddenIfNotAdmin() {
//        UserRequest request = new UserRequest();
//        request.setUsername("updatedself");
//
//        String clientToken = generateToken(clientId, UserRole.ROLE_CLIENT.name());
//
//        webTestClient.put()
//                .uri("/api/v1/users/{id}", clientId)
//                .headers(h -> h.setBearerAuth(clientToken))
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void getAllUsers_emptyDatabase_shouldReturnEmptyList() {
//        userRepository.deleteAll().block();
//
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.get()
//                .uri("/api/v1/users?page=0&size=10")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(UserDto.class)
//                .hasSize(0);
//    }
//
//    @Test
//    void getAllUsers_defaultPagination_shouldUseDefaults() {
//        String adminToken = generateToken(adminId, UserRole.ROLE_ADMIN.name());
//
//        webTestClient.get()
//                .uri("/api/v1/users")
//                .headers(h -> h.setBearerAuth(adminToken))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(UserDto.class)
//                .hasSize(3);
//    }
//}