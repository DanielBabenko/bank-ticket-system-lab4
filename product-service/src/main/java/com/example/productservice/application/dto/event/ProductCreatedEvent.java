package com.example.productservice.application.dto.event;

import java.time.Instant;
import java.util.UUID;

public class ProductCreatedEvent {

    private final UUID eventId;
    private final Instant timestamp;
    private final UUID productId;
    private final String productName;

    public ProductCreatedEvent(UUID productId, String productName) {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.productId = productId;
        this.productName = productName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }
}
