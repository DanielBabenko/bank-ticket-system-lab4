package com.example.userservice.application.usecases;

import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Service
public class DeleteUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(DeleteUserUseCase.class);

    private final UserRepository userRepository;
    private final KafkaSender<String, String> kafkaSender;
    private final AdminRoleValidator validator;

    @Value("${spring.kafka.topics.user-deleted:user.deleted}")
    private String userDeletedTopic;

    public DeleteUserUseCase(UserRepository userRepository, KafkaSender<String, String> kafkaSender, AdminRoleValidator validator) {
        this.userRepository = userRepository;
        this.kafkaSender = kafkaSender;
        this.validator = validator;
    }

    public Mono<Void> delete(UUID userId) {
        return validator.validateAdmin()
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
}
