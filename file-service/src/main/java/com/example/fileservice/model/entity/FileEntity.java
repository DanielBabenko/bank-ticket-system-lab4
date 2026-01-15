package com.example.fileservice.model.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    private String contentType;

    private Long size;

    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private UUID uploadedBy;

    private Instant uploadedAt;

    @ElementCollection
    @CollectionTable(name = "file_applications",
            joinColumns = @JoinColumn(name = "file_id"))
    @Column(name = "application_id")
    private Set<UUID> applicationIds = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        uploadedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UUID uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Set<UUID> getApplicationIds() {
        return applicationIds;
    }

    public void setApplicationIds(Set<UUID> applicationIds) {
        this.applicationIds = applicationIds;
    }
}