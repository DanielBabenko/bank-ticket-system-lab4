package com.example.tagservice.adapters.inbound.messaging.kafka;

import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.CreateOrGetTagsBatchUseCasePort;
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
public class TagEventListener {

    private static final Logger log = LoggerFactory.getLogger(TagEventListener.class);

    private final CreateOrGetTagsBatchUseCasePort createOrGetTagsBatchUseCase;
    private final ObjectMapper objectMapper;

    public TagEventListener(CreateOrGetTagsBatchUseCasePort createOrGetTagsBatchUseCase,
                            ObjectMapper objectMapper) {
        this.createOrGetTagsBatchUseCase = createOrGetTagsBatchUseCase;
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
            TagEvent event = objectMapper.readValue(message, TagEvent.class);

            if (event.getTagNames() == null || event.getTagNames().isEmpty()) {
                log.warn("Event {} contains no tags, skipping", eventId);
                return;
            }

            List<Tag> created = createOrGetTagsBatchUseCase.createOrGetTags(event.getTagNames());

            log.info("Successfully processed {} tags from event {}: {}", created.size(), eventId,
                    created.stream().map(Tag::getName).toList());
        } catch (Exception e) {
            log.error("Error processing tag event: {}", e.getMessage(), e);
            // Можно добавить повторную отправку в DLQ/логирование/метрики
        }
    }
}
