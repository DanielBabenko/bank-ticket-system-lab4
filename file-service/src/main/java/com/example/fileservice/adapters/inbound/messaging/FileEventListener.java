package com.example.fileservice.adapters.inbound.messaging;

import com.example.fileservice.adapters.inbound.messaging.event.FileEvent;
import com.example.fileservice.domain.port.inbound.GetFilesBatchUseCasePort;
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
import java.util.UUID;

@Component
public class FileEventListener {

    private static final Logger log = LoggerFactory.getLogger(FileEventListener.class);

    private final GetFilesBatchUseCasePort getFilesBatchUseCase;
    private final ObjectMapper objectMapper;

    public FileEventListener(GetFilesBatchUseCasePort getFilesBatchUseCase, ObjectMapper objectMapper) {
        this.getFilesBatchUseCase = getFilesBatchUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {"${spring.kafka.topics.file-attach-request:file.attach.request}"},
            groupId = "${spring.kafka.consumer.group-id:file-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleFileRequest(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String eventId,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Received file event from topic: {}, eventId: {}", topic, eventId);

        try {
            FileEvent event = objectMapper.readValue(message, FileEvent.class);

            if (event.getFileIds() == null || event.getFileIds().isEmpty()) {
                log.warn("Event {} contains no files, skipping", eventId);
                return;
            }

            List<UUID> realIds = getFilesBatchUseCase.getFilesBatch(event.getFileIds());
            log.info("Processed event {}, real files count: {}", eventId, realIds.size());
        } catch (Exception e) {
            log.error("Error processing file event: {}", e.getMessage(), e);
        }
    }
}
