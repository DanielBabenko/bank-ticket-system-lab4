package com.example.applicationservice.application.dto;

import java.time.Instant;
import java.util.UUID;

public class ApplicationInfoDto {
    private UUID id;
    private UUID applicantId;
    private UUID productId;
    private String status;
    private Instant createdAt;

    public ApplicationInfoDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getApplicantId() { return applicantId; }
    public void setApplicantId(UUID applicantId) { this.applicantId = applicantId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
