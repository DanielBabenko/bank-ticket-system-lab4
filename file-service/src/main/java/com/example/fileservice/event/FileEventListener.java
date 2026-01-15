package com.example.fileservice.event;

import com.example.fileservice.model.entity.File;
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

import java.util.List;

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
            topics = {"${spring.kafka.topics.file-attach-request:file.attach.request}"},
            groupId = "${spring.kafka.consumer.group-id:file-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleFileRequest(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String eventId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Received file event from topic: {}, eventId: {}", topic, eventId);

        try {
            // Десериализуем с использованием нового конструктора
            FileEvent event = objectMapper.readValue(message, FileEvent.class);

            log.info("Processing {} for {} files, applicationId: {}, actorId: {}, timestamp: {}, files: {}",
                    event.getEventType(),
                    event.getFileIds().size(),
                    event.getApplicationId(),
                    event.getActorId(),
                    event.getTimestamp(),
                    event.getFileIds());

            // Используем готовый метод FileService
            List<File> createdFiles = fileService.getFiles(event.getFileIds());

            log.info("Successfully processed {} files: {}", createdFiles.size(),
                    createdFiles.stream().map(File::getName).toList());

        } catch (Exception e) {
            log.error("Error processing file event: {}", e.getMessage(), e);
        }
    }
}
