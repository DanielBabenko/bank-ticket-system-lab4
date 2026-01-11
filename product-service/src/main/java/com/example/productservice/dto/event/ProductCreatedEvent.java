package com.example.productservice.dto.event;

import java.time.Instant;
import java.util.UUID;

public class ProductCreatedEvent {
    private UUID eventId;
    private Instant timestamp;
    private String eventType;
    private UUID productId;
    private String productName;

    public ProductCreatedEvent(UUID productId, String productName) {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.eventType = "PRODUCT_CREATED";
        this.productId = productId;
        this.productName = productName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}