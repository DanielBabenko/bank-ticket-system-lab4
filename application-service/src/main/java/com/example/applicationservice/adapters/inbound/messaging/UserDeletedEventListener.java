package com.example.applicationservice.adapters.inbound.messaging;

import com.example.applicationservice.domain.port.inbound.DeleteApplicationsByUserIdUseCasePort;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserDeletedEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserDeletedEventListener.class);

    private final DeleteApplicationsByUserIdUseCasePort deleteByUserPort;

    public UserDeletedEventListener(DeleteApplicationsByUserIdUseCasePort deleteByUserPort) {
        this.deleteByUserPort = deleteByUserPort;
    }

    @KafkaListener(topics = "${spring.kafka.topics.user-deleted:user.deleted}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void handle(@Payload String userIdString) {
        try {
            UUID userId = UUID.fromString(userIdString);
            log.info("Received user.deleted for {}", userId);
            deleteByUserPort.deleteApplicationsByUserId(userId);
            log.info("Deleted applications for user {}", userId);
        } catch (Exception e) {
            log.error("Error handling user.deleted message: {}", userIdString, e);
        }
    }
}
