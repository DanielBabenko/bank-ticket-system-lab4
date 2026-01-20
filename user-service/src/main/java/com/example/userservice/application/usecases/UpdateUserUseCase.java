package com.example.userservice.application.usecases;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.model.enums.UserRole;
import com.example.userservice.domain.repository.UserRepository;
import com.example.userservice.presentation.dto.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(UpdateUserUseCase.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRoleValidator validator;
    private final ToUserDto toUserDto;

    public UpdateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, AdminRoleValidator validator, ToUserDto toUserDto) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.toUserDto = toUserDto;
    }

    @Transactional
    public Mono<UserDto> update(UUID userId, UserRequest req) {
        return validator.validateAdmin()
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

                    return userRepository.save(user).map(toUserDto::toDto).doOnSuccess(dto -> log.info("User updated: {}", dto.getId()));
                });
    }

    @Transactional
    public Mono<Void> promoteToManager(UUID userId) {
        return validator.validateAdmin()
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
        return validator.validateAdmin()
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
}
