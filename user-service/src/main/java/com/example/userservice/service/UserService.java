package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.UserRequest;
import com.example.userservice.exception.*;
import com.example.userservice.model.entity.User;
import com.example.userservice.model.enums.UserRole;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaSender<String, String> kafkaSender;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, KafkaSender<String, String> kafkaSender, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaSender = kafkaSender;
    }

    @Value("${spring.kafka.topics.user-deleted:user.deleted}")
    private String userDeletedTopic;

    @Transactional
    public Mono<UserDto> create(UserRequest req) {
        if (req == null) {
            throw new BadRequestException("Request is required");
        }

        String username = req.getUsername() != null ? req.getUsername().trim() : null;
        String email = req.getEmail() != null ? req.getEmail().trim().toLowerCase() : null;
        String password = req.getPassword();

        if (username == null || email == null || password == null) {
            throw new BadRequestException("Username, email and password are required");
        }

        return userRepository.existsByUsername(username)
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        throw new ConflictException("Username already in use");
                    }
                    return userRepository.existsByEmail(email);
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        throw new ConflictException("Email already in use");
                    }

                    User user = new User();
                    user.setId(UUID.randomUUID());
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPasswordHash(passwordEncoder.encode(password));
                    user.setRole(UserRole.ROLE_CLIENT);
                    user.setCreatedAt(Instant.now());

                    return userRepository.save(user)
                            .map(this::toDto)
                            .doOnSuccess(dto -> log.info("User created: {}", dto.getUsername()));
                });
    }

    public Flux<UserDto> findAll(int page, int size) {
        if (size > 50) {
            return Flux.error(new BadRequestException("Page size cannot exceed 50"));
        }

        return userRepository.findAll()
                .skip((long) page * size)
                .take(size)
                .map(this::toDto);
    }

    public Mono<UserDto> findById(UUID id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + id)))
                .map(this::toDto);
    }

    @Transactional
    public Mono<UserDto> update(UUID userId, UserRequest req) {
        return validateAdmin()
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(user -> {
                    if (req.getUsername() != null) {
                        user.setUsername(req.getUsername().trim());
                    }
                    if (req.getEmail() != null) {
                        user.setEmail(req.getEmail().trim().toLowerCase());
                    }
                    if (req.getPassword() != null) {
                        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
                    }
                    user.setUpdatedAt(Instant.now());

                    return userRepository.save(user).map(this::toDto).doOnSuccess(dto -> log.info("User updated: {}", dto.getId()));
                });
    }

    @Transactional
    public Mono<Void> delete(UUID userId) {
        return validateAdmin()
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(user -> {
                    log.info("Deleting user {}", userId);

                    String message = userId.toString();

                    SenderRecord<String, String, String> kafkaMessage = SenderRecord
                            .create(userDeletedTopic,
                                    null,
                                    System.currentTimeMillis(),
                                    userId.toString(),
                                    message,
                                    null);

                    return kafkaSender.send(Mono.just(kafkaMessage))
                            .next()
                            .flatMap(result -> {
                                if (result.exception() != null) {
                                    return Mono.error(new RuntimeException(
                                            "Failed to send Kafka message for user " + userId,
                                            result.exception()
                                    ));
                                }
                                log.debug("Kafka message sent successfully for user {}", userId);
                                return userRepository.delete(user);
                            })
                            .doOnSuccess(unused -> log.info("User {} deleted successfully", userId))
                            .doOnError(e -> log.error("Failed to process deletion for user {}: {}", userId, e.getMessage()));
                });
    }

    @Transactional
    public Mono<Void> promoteToManager(UUID userId) {
        return validateAdmin()
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(user -> {
                    if (user.getRole() != UserRole.ROLE_MANAGER) {
                        user.setRole(UserRole.ROLE_MANAGER);
                        user.setUpdatedAt(Instant.now());
                        return userRepository.save(user).then();
                    }
                    return Mono.empty();
                }).doOnSuccess(v -> log.info("User {} promoted to MANAGER", userId));
    }

    @Transactional
    public Mono<Void> demoteToClient(UUID userId) {
        return validateAdmin()
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(user -> {
                    if (user.getRole() != UserRole.ROLE_CLIENT) {
                        user.setRole(UserRole.ROLE_CLIENT);
                        user.setUpdatedAt(Instant.now());
                        return userRepository.save(user).then();
                    }
                    return Mono.empty();
                }).doOnSuccess(v -> log.info("User {} demoted to CLIENT", userId));
    }

    public Mono<Long> count() {
        return userRepository.count();
    }

    public Mono<User> validateAdmin() {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new UnauthorizedException("Unauthorized")))
                .flatMap(ctx -> {
                    if (ctx.getAuthentication() == null || ctx.getAuthentication().getPrincipal() == null) {
                        return Mono.error(new UnauthorizedException("Unauthorized"));
                    }
                    String principal = ctx.getAuthentication().getPrincipal().toString();
                    UUID actorId;
                    try {
                        actorId = UUID.fromString(principal);
                    } catch (IllegalArgumentException ex) {
                        return Mono.error(new UnauthorizedException("Invalid principal"));
                    }

                    return userRepository.findById(actorId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Actor not found: " + actorId)))
                            .flatMap(actor -> {
                                if (actor.getRole() != UserRole.ROLE_ADMIN) {
                                    return Mono.error(new ForbiddenException("Only ADMIN can perform this action"));
                                }
                                return Mono.just(actor);
                            });
                });
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}