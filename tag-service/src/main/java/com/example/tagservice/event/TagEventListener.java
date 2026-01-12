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
            // Десериализуем с использованием нового конструктора
            TagEvent event = objectMapper.readValue(message, TagEvent.class);

            log.info("Processing {} for {} tags, applicationId: {}, actorId: {}, timestamp: {}, tags: {}",
                    event.getEventType(),
                    event.getTagNames().size(),
                    event.getApplicationId(),
                    event.getActorId(),
                    event.getTimestamp(),
                    event.getTagNames());

            // Используем готовый метод TagService
            List<Tag> createdTags = tagService.createOrGetTags(event.getTagNames());

            log.info("Successfully processed {} tags: {}", createdTags.size(),
                    createdTags.stream().map(Tag::getName).toList());

        } catch (Exception e) {
            log.error("Error processing tag event: {}", e.getMessage(), e);
        }
    }
}