package com.example.applicationservice.event;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

public class TagEvent {
    private UUID eventId;
    private String eventType; // "TAG_CREATE_REQUEST" или "TAG_ATTACH_REQUEST"
    private UUID applicationId; // Для attachTags
    private UUID actorId;
    private List<String> tagNames;
    private Instant timestamp;

    // Конструкторы, геттеры, сеттеры
    public TagEvent() {}

    public TagEvent(UUID eventId, String eventType, List<String> tagNames) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.tagNames = tagNames;
        this.timestamp = Instant.now();
    }

    public TagEvent(UUID eventId, String eventType, UUID applicationId, UUID actorId, List<String> tagNames) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.applicationId = applicationId;
        this.actorId = actorId;
        this.tagNames = tagNames;
        this.timestamp = Instant.now();
    }

    // Геттеры и сеттеры
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


}