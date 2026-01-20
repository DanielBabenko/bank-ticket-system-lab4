package com.example.tagservice.adapters.inbound.messaging.kafka;

import java.util.List;
import java.util.UUID;

/**
 * DTO для десериализации Kafka-сообщения.
 */
public class TagEvent {
    private UUID eventId;
    private String eventType; // "TAG_CREATE_REQUEST" или "TAG_ATTACH_REQUEST"
    private UUID applicationId;
    private UUID actorId;
    private List<String> tagNames;
    private long timestamp;

    public TagEvent() {}

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }

    public List<String> getTagNames() { return tagNames; }
    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}