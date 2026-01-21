package com.example.fileservice.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "file")
public class FileJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String originalName;

    @Column(nullable = false, length = 255)
    private String storageKey;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, length = 50)
    private String extension;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column(nullable = false)
    private UUID uploaderId;

    @Column(nullable = false)
    private String bucketName = "files";

    @Column
    private String description;

    @Transient
    private Set<UUID> applicationIds = new HashSet<>();

    public FileJpaEntity() {}

    // getters / setters omitted for brevity (same fields)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public UUID getUploaderId() { return uploaderId; }
    public void setUploaderId(UUID uploaderId) { this.uploaderId = uploaderId; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<UUID> getApplicationIds() { return applicationIds; }
    public void setApplicationIds(Set<UUID> applicationIds) { this.applicationIds = applicationIds; }
}
