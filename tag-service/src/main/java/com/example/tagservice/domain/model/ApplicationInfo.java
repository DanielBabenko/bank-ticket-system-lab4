package com.example.tagservice.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Легковесный доменный DTO, описывающий информацию об application,
 * возвращаемую из внешний сервисов (application-service).
 * Используется в доменных взаимодействиях через порт ApplicationServicePort.
 */
public class ApplicationInfo {
    private UUID id;
    private UUID applicantId;
    private UUID productId;
    private String status;
    private Instant createdAt;

    public ApplicationInfo() {}

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
