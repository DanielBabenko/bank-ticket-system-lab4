package com.example.applicationservice.dto;

import java.util.UUID;

public class AttachFileRequest {
    private UUID applicationId;
    private UUID userId;

    public AttachFileRequest(UUID id, UUID actorId) {
        this.applicationId = id;
        this.userId = actorId;
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
}