package com.example.applicationservice.event;

import com.example.applicationservice.service.ApplicationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class UserDeletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(UserDeletedEventListener.class);
    private final ApplicationService applicationService;

    public UserDeletedEventListener(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.user-deleted:user.deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserDeleted(
            @Payload String userIdString,
            Acknowledgment acknowledgment) {

        try {
            UUID userId = UUID.fromString(userIdString);
            log.info("Processing deletion for userId: {}", userId);

            applicationService.deleteApplicationsByUserId(userId)
                    .doOnSuccess(v -> acknowledgment.acknowledge())
                    .subscribe();

        } catch (Exception e) {
            log.error("Invalid userId: {}", userIdString, e);
            acknowledgment.acknowledge();
        }
    }
}