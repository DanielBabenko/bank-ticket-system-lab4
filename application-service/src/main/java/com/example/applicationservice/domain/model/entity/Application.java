package com.example.applicationservice.domain.model.entity;

import com.example.applicationservice.domain.model.enums.ApplicationStatus;

import java.time.Instant;
import java.util.*;

/**
 * Доменная модель Application — чистая от фреймворков.
 * (В adapters/outbound/persistence будет JPA-entity с аннотациями).
 */
public class Application {

    private UUID id;
    private UUID applicantId;
    private UUID productId;
    private ApplicationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private Set<UUID> files = new HashSet<>();
    private Set<String> tags = new HashSet<>();
    private List<ApplicationHistory> history = new ArrayList<>();

    public Application() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getApplicantId() { return applicantId; }
    public void setApplicantId(UUID applicantId) { this.applicantId = applicantId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Set<UUID> getFiles() { return files; }
    public void setFiles(Set<UUID> files) {
        this.files = (files == null) ? new HashSet<>() : new HashSet<>(files);
    }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) {
        this.tags = (tags == null) ? new HashSet<>() : new HashSet<>(tags);
    }

    public List<ApplicationHistory> getHistory() { return history; }
    public void setHistory(List<ApplicationHistory> history) {
        this.history = (history == null) ? new ArrayList<>() : new ArrayList<>(history);
    }

    // Удобные операции
    public void addFiles(Collection<UUID> fileIds) {
        if (fileIds != null) this.files.addAll(fileIds);
    }

    public void removeFiles(Collection<UUID> fileIds) {
        if (fileIds != null) fileIds.forEach(this.files::remove);
    }

    public void addTags(Collection<String> tagNames) {
        if (tagNames != null) this.tags.addAll(tagNames);
    }

    public void removeTags(Collection<String> tagNames) {
        if (tagNames != null) tagNames.forEach(this.tags::remove);
    }
}

