package com.example.fileservice.adapters.inbound.messaging.event;

import java.util.List;
import java.util.UUID;

public class FileEvent {
    private UUID eventId;
    private String eventType;
    private UUID applicationId;
    private UUID actorId;
    private List<UUID> fileIds;
    private long timestamp;

    // Конструкторы, геттеры, сеттеры
    public FileEvent() {}

    public FileEvent(UUID eventId, String eventType, List<UUID> fileIds) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.fileIds = fileIds;
        this.timestamp = System.currentTimeMillis();
    }

    public FileEvent(UUID eventId, String eventType, UUID applicationId, UUID actorId, List<UUID> fileIds) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.applicationId = applicationId;
        this.actorId = actorId;
        this.fileIds = fileIds;
        this.timestamp = System.currentTimeMillis();
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

    public List<UUID> getFileIds() { return fileIds; }
    public void setFileIds(List<UUID> fileIds) { this.fileIds = fileIds; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
