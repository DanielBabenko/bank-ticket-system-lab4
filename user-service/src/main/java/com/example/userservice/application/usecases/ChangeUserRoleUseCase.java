package com.example.userservice.application.usecases;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.dto.UserRequest;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.application.validator.AdminRoleValidator;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.model.enums.UserRole;
import com.example.userservice.domain.ports.inbound.AdminRoleValidatorPort;
import com.example.userservice.domain.ports.inbound.ChangeUserRoleUseCasePort;
import com.example.userservice.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public class ChangeUserRoleUseCase implements ChangeUserRoleUseCasePort {
    private static final Logger log = LoggerFactory.getLogger(ChangeUserRoleUseCase.class);

    private final UserRepository userRepository;
    private final AdminRoleValidator validator;

    public ChangeUserRoleUseCase(UserRepository userRepository, AdminRoleValidator validator) {
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @Override
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

    @Override
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
