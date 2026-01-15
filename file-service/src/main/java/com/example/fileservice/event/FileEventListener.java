package com.example.fileservice.event;

import com.example.fileservice.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class FileEventListener {
    private static final Logger log = LoggerFactory.getLogger(FileEventListener.class);

    private final FileService fileService;
    private final ObjectMapper objectMapper;

    public FileEventListener(FileService fileService, ObjectMapper objectMapper) {
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.file-attach-request:file.attach.request}",
            groupId = "${spring.kafka.consumer.group-id:file-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleFileAttachRequest(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String eventId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Received file attach event from topic: {}, eventId: {}", topic, eventId);

        try {
            FileEvent event = objectMapper.readValue(message, FileEvent.class);

            log.info("Processing FILE_ATTACH_REQUEST for application: {}, files: {}",
                    event.getApplicationId(),
                    event.getFileIds() != null ? event.getFileIds().size() : 0);

            // Используем метод FileService для прикрепления файлов
            if (event.getFileIds() != null && !event.getFileIds().isEmpty()) {
                for (UUID fileId : event.getFileIds()) {
                    try {
                        fileService.attachToApplication(
                                fileId,
                                event.getApplicationId(),
                                event.getUserId()
                        );
                    } catch (Exception e) {
                        log.error("Failed to attach file {} to application {}: {}",
                                fileId, event.getApplicationId(), e.getMessage());
                    }
                }
            }

            log.info("Successfully processed file attach event: {}", eventId);

        } catch (Exception e) {
            log.error("Error processing file attach event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.file-detach-request:file.detach.request}",
            groupId = "${spring.kafka.consumer.group-id:file-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleFileDetachRequest(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String eventId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Received file detach event from topic: {}, eventId: {}", topic, eventId);

        try {
            FileEvent event = objectMapper.readValue(message, FileEvent.class);

            log.info("Processing FILE_DETACH_REQUEST for application: {}, files: {}",
                    event.getApplicationId(),
                    event.getFileIds() != null ? event.getFileIds().size() : 0);

            // Используем метод FileService для открепления файлов
            if (event.getFileIds() != null && !event.getFileIds().isEmpty()) {
                for (UUID fileId : event.getFileIds()) {
                    try {
                        fileService.detachFromApplication(
                                fileId,
                                event.getApplicationId(),
                                event.getUserId()
                        );
                    } catch (Exception e) {
                        log.error("Failed to detach file {} from application {}: {}",
                                fileId, event.getApplicationId(), e.getMessage());
                    }
                }
            }

            log.info("Successfully processed file detach event: {}", eventId);

        } catch (Exception e) {
            log.error("Error processing file detach event: {}", e.getMessage(), e);
        }
    }
}