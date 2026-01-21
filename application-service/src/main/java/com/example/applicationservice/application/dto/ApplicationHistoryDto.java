package com.example.applicationservice.application.dto;

import com.example.applicationservice.domain.model.enums.ApplicationStatus;
import com.example.applicationservice.domain.model.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public class ApplicationHistoryDto {
    private UUID id;
    private UUID applicationId;
    private ApplicationStatus oldStatus;
    private ApplicationStatus newStatus;
    private UserRole changedByRole;
    private Instant changedAt;

    public ApplicationHistoryDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public ApplicationStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(ApplicationStatus oldStatus) { this.oldStatus = oldStatus; }

    public ApplicationStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ApplicationStatus newStatus) { this.newStatus = newStatus; }

    public UserRole getChangedByRole() { return changedByRole; }
    public void setChangedByRole(UserRole changedByRole) { this.changedByRole = changedByRole; }

    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
}
