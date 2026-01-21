package com.example.userservice.application.usecases;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.application.validator.AdminRoleValidator;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.model.enums.UserRole;
import com.example.userservice.domain.ports.inbound.UpdateUserUseCasePort;
import com.example.userservice.domain.repository.UserRepository;
import com.example.userservice.application.dto.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public class UpdateUserUseCase implements UpdateUserUseCasePort {
    private static final Logger log = LoggerFactory.getLogger(UpdateUserUseCase.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRoleValidator validator;
    private final UserMapper userMapper;

    public UpdateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, AdminRoleValidator validator, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.userMapper = userMapper;
    }

    @Override
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

                    return userRepository.save(user).map(userMapper::toDto).doOnSuccess(dto -> log.info("User updated: {}", dto.getId()));
                });
    }
}
