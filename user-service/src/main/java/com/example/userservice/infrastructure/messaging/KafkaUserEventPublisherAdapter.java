package com.example.userservice.infrastructure.messaging;

import com.example.userservice.domain.ports.UserEventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Component
public class KafkaUserEventPublisherAdapter implements UserEventPublisherPort {
    private final KafkaSender<String, String> kafkaSender;
    private final String userDeletedTopic;

    public KafkaUserEventPublisherAdapter(
            KafkaSender<String, String> kafkaSender,
            @Value("${spring.kafka.topics.user-deleted:user.deleted}") String userDeletedTopic) {
        this.kafkaSender = kafkaSender;
        this.userDeletedTopic = userDeletedTopic;
    }

    @Override
    public Mono<Void> publishUserDeletedEvent(UUID userId) {
        SenderRecord<String, String, String> kafkaMessage = SenderRecord
                .create(userDeletedTopic, null, System.currentTimeMillis(),
                        userId.toString(), userId.toString(), null);

        return kafkaSender.send(Mono.just(kafkaMessage))
                .next()
                .doOnNext(result -> {
                    if (result.exception() != null) {
                        throw new RuntimeException("Failed to send Kafka message: " + result.exception().getMessage(),
                                result.exception());
                    }
                })
                .then();
    }
}

