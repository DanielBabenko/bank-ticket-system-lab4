package com.example.applicationservice.adapters.outbound.event;

import com.example.applicationservice.domain.event.FileEvent;
import com.example.applicationservice.domain.event.TagEvent;
import com.example.applicationservice.domain.port.outbound.EventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);

    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.tag-create-request:tag.create.request}")
    private String tagCreateTopic;

    @Value("${spring.kafka.topics.tag-attach-request:tag.attach.request}")
    private String tagAttachTopic;

    @Value("${spring.kafka.topics.file-attach-request:file.attach.request}")
    private String fileAttachTopic;

    public KafkaEventPublisherAdapter(KafkaSender<String, String> kafkaSender, ObjectMapper objectMapper) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishTagCreateRequest(TagEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            SenderRecord<String, String, String> record = SenderRecord.create(tagCreateTopic, null, System.currentTimeMillis(),
                    event.getEventId().toString(), message, null);
            kafkaSender.send(Mono.just(record))
                    .doOnNext(r -> {
                        if (r.exception() == null) {
                            log.info("Tag create request sent: {}", event.getEventId());
                        } else {
                            log.error("Failed sending tag create request: {}", r.exception().getMessage());
                        }
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Error serializing TagEvent", e);
        }
    }

    @Override
    public void publishTagAttachRequest(TagEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            SenderRecord<String, String, String> record = SenderRecord.create(tagAttachTopic, null, System.currentTimeMillis(),
                    event.getEventId().toString(), message, null);
            kafkaSender.send(Mono.just(record)).subscribe();
        } catch (Exception e) {
            log.error("Error serializing TagEvent", e);
        }
    }

    @Override
    public void publishFileAttachRequest(FileEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            SenderRecord<String, String, String> record = SenderRecord.create(fileAttachTopic, null, System.currentTimeMillis(),
                    event.getEventId().toString(), message, null);
            kafkaSender.send(Mono.just(record)).subscribe();
        } catch (Exception e) {
            log.error("Error serializing FileEvent", e);
        }
    }
}
