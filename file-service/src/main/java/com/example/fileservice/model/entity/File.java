package com.example.fileservice.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "file")
public class File {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String originalName;

    @Column(nullable = false, length = 255)
    private String storageKey; // Уникальный ключ файла в хранилище

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long size; // Размер в байтах

    @Column(nullable = false, length = 50)
    private String extension;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column(nullable = false)
    private UUID uploaderId;

    @Column
    private String uploaderUsername;

    @Column(nullable = false)
    private String bucketName = "files"; // Имя бакета в MinIO

    @Column
    private String description;

    @Transient
    private Set<UUID> applicationIds = new HashSet<>();

    // Конструкторы
    public File() {}

    public File(UUID id, String originalName, Long size, String mimeType,
                UUID uploaderId, String uploaderUsername) {
        this.id = id;
        this.originalName = originalName;
        this.size = size;
        this.mimeType = mimeType;
        this.uploaderId = uploaderId;
        this.uploaderUsername = uploaderUsername;
        this.uploadDate = LocalDateTime.now();
        this.extension = extractExtension(originalName);
        this.storageKey = generateStorageKey(id, extension);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String generateStorageKey(UUID fileId, String extension) {
        if (extension == null || extension.isEmpty()) {
            return fileId.toString();
        }
        return fileId.toString() + "." + extension;
    }

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
        this.extension = extractExtension(originalName);
    }

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

    public String getUploaderUsername() { return uploaderUsername; }
    public void setUploaderUsername(String uploaderUsername) { this.uploaderUsername = uploaderUsername; }

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<UUID> getApplicationIds() { return applicationIds; }
    public void setApplicationIds(Set<UUID> applicationIds) { this.applicationIds = applicationIds; }
}
