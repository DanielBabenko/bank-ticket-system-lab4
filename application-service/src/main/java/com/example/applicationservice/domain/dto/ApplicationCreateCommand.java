package com.example.applicationservice.domain.dto;

import java.util.List;
import java.util.UUID;

/**
 * Команда для создания заявки (domain-level).
 * Используется как входной параметр в CreateApplicationUseCasePort.
 */
public class ApplicationCreateCommand {
    private UUID applicantId;
    private UUID productId;
    private List<UUID> files;
    private List<String> tags;

    public ApplicationCreateCommand() {}

    public UUID getApplicantId() { return applicantId; }
    public void setApplicantId(UUID applicantId) { this.applicantId = applicantId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public List<UUID> getFiles() { return files; }
    public void setFiles(List<UUID> files) { this.files = files; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
