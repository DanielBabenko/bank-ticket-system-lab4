package com.example.fileservice.dto;

import java.util.List;
import java.util.UUID;

public class AttachFilesRequest {
    private UUID applicationId;
    private java.util.List<UUID> fileIds;

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public List<UUID> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<UUID> fileIds) {
        this.fileIds = fileIds;
    }
}
