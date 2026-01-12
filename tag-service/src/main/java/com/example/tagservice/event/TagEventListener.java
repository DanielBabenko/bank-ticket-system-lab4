package com.example.tagservice.event;

import com.example.tagservice.dto.ApplicationInfoDto;
import com.example.tagservice.dto.TagDto;
import com.example.tagservice.model.entity.Tag;
import com.example.tagservice.service.TagService;
import com.fasterxml.jackson.databind.JsonNode;
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
public class TagEventListener {
    private static final Logger log = LoggerFactory.getLogger(TagEventListener.class);

    private final TagService tagService;
    private final ObjectMapper objectMapper;

    public TagEventListener(TagService tagService, ObjectMapper objectMapper) {
        this.tagService = tagService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {"${spring.kafka.topics.tag-create-request:tag.create.request}",
                    "${spring.kafka.topics.tag-attach-request:tag.attach.request}"},
            groupId = "${spring.kafka.consumer.group-id:tag-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleTagRequest(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String eventId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Received tag event from topic: {}, eventId: {}", topic, eventId);

        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("eventType").asText();
            List<String> tagNames = objectMapper.convertValue(
                    jsonNode.get("tagNames"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            UUID applicationId = jsonNode.has("applicationId") ?
                    UUID.fromString(jsonNode.get("applicationId").asText()) : null;
            UUID actorId = jsonNode.has("actorId") ?
                    UUID.fromString(jsonNode.get("actorId").asText()) : null;

            log.info("Processing {} for {} tags, applicationId: {}, tags: {}",
                    eventType, tagNames.size(), applicationId, tagNames);

            // Используем готовый метод TagService
            List<Tag> createdTags = tagService.createOrGetTags(tagNames);

            log.info("Successfully processed {} tags: {}", createdTags.size(),
                    createdTags.stream().map(Tag::getName).toList());

            // Если нужно отправить подтверждение обратно в application-service
            // можно добавить отправку ответного события
            if ("TAG_CREATE_REQUEST".equals(eventType) && applicationId != null) {
                sendTagCreatedConfirmation(applicationId, createdTags);
            }

        } catch (Exception e) {
            log.error("Error processing tag event: {}", e.getMessage(), e);
        }
    }

    private void sendTagCreatedConfirmation(UUID applicationId, List<Tag> createdTags) {
        // Можно отправить подтверждение, если это необходимо
        // Например, если application-service хочет знать, что теги точно созданы
        log.debug("Tags created for application {}: {}",
                applicationId, createdTags.stream().map(Tag::getName).toList());
    }
}