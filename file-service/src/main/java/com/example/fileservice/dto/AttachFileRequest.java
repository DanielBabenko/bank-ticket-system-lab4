package com.example.fileservice.dto;

import java.util.UUID;

public class AttachFileRequest {
    private UUID applicationId;
    private UUID userId;

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }
}
