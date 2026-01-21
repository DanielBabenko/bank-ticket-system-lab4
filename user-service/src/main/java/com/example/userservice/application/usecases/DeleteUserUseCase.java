package com.example.userservice.application.usecases;

import com.example.userservice.application.validator.AdminRoleValidator;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.ports.UserEventPublisherPort;
import com.example.userservice.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class DeleteUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(DeleteUserUseCase.class);

    private final UserRepository userRepository;
    private final UserEventPublisherPort eventPublisher;
    private final AdminRoleValidator validator;

    public DeleteUserUseCase(
            UserRepository userRepository,
            UserEventPublisherPort eventPublisher,
            AdminRoleValidator validator) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.validator = validator;
    }

    public Mono<Void> delete(UUID userId) {
        return validator.validateAdmin()
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(user -> {
                    log.info("Deleting user {}", userId);

                    return eventPublisher.publishUserDeletedEvent(userId)
                            .then(userRepository.delete(user))
                            .doOnSuccess(unused -> log.info("User {} deleted successfully", userId));
                });
    }
}
