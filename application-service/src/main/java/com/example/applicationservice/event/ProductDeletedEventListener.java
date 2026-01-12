package com.example.applicationservice.event;

import com.example.applicationservice.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductDeletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(ProductDeletedEventListener.class);
    private final ApplicationService applicationService;

    public ProductDeletedEventListener(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.product-deleted:product.deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleProductDeleted(
            @Payload String productIdString,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[Partition:{} Offset:{}] Received productId: {}",
                partition, offset, productIdString);

        try {
            UUID productId = UUID.fromString(productIdString);

            applicationService.deleteApplicationsByProductId(productId)
                    .doOnSuccess(v -> {
                        log.info("Successfully deleted application(s) for productId: {}",
                                productId);
                    })
                    .doOnError(e -> {
                        log.error("Failed to process ProductDeletedEvent for productId: {}. Error: {}",
                                productId, e.getMessage());
                    })
                    .block();

        } catch (Exception e) {
            log.error("Invalid productId format: {}", productIdString, e);
        }
    }
}