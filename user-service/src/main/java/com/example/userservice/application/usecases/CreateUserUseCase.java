package com.example.userservice.application.usecases;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.application.mapper.UserMapper;
import com.example.userservice.domain.exception.BadRequestException;
import com.example.userservice.domain.exception.ConflictException;
import com.example.userservice.domain.model.entity.User;
import com.example.userservice.domain.model.enums.UserRole;
import com.example.userservice.domain.repository.UserRepository;
import com.example.userservice.adapters.presentation.dto.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(CreateUserUseCase.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

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
                            .map(userMapper::toDto)
                            .doOnSuccess(dto -> log.info("User created: {}", dto.getUsername()));
                });
    }
}
