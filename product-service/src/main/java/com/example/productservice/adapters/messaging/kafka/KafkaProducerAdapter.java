package com.example.productservice.adapters.messaging.kafka;

import com.example.productservice.domain.port.outbound.ProductEventPublisherPort;
import org.springframework.stereotype.Component;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Component
public class KafkaProducerAdapter implements ProductEventPublisherPort {

    private final KafkaSender<String, String> sender;

    public KafkaProducerAdapter(KafkaSender<String, String> sender) {
        this.sender = sender;
    }

    @Override
    public void publishProductDeleted(UUID productId) {
        sender.send(
                reactor.core.publisher.Mono.just(
                        SenderRecord.create("product.deleted", null, null, productId.toString(), productId.toString(), null)
                )
        ).subscribe();
    }
}
