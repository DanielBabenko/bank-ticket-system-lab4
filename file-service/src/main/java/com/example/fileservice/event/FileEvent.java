package com.example.fileservice.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FileEvent {
    private UUID eventId;
    private String eventType; // "FILE_ATTACHED", "FILE_DETACHED"
    private UUID fileId;
    private UUID applicationId;
    private UUID userId;

    private List<UUID> fileIds; // для batch операций
    private Instant timestamp;

    public FileEvent(String eventType, UUID applicationId, UUID userId, List<UUID> fileIds) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.applicationId = applicationId;
        this.userId = userId;
        this.fileIds = fileIds;
        this.timestamp = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<UUID> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<UUID> fileIds) {
        this.fileIds = fileIds;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}