package com.example.applicationservice.adapters.inbound.messaging;

import com.example.applicationservice.domain.port.inbound.DeleteApplicationsByProductIdUseCasePort;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductDeletedEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProductDeletedEventListener.class);

    private final DeleteApplicationsByProductIdUseCasePort deleteByProductPort;

    public ProductDeletedEventListener(DeleteApplicationsByProductIdUseCasePort deleteByProductPort) {
        this.deleteByProductPort = deleteByProductPort;
    }

    @KafkaListener(topics = "${spring.kafka.topics.product-deleted:product.deleted}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void handle(@Payload String productIdString) {
        try {
            UUID productId = UUID.fromString(productIdString);
            log.info("Received product.deleted for {}", productId);
            // call sync method on port (already transactional via decorator)
            deleteByProductPort.deleteApplicationsByProductId(productId);
            log.info("Deleted applications for product {}", productId);
        } catch (Exception e) {
            log.error("Error handling product.deleted message: {}", productIdString, e);
            // consider dead-lettering / retry in production
        }
    }
}
