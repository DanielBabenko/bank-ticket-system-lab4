package com.example.fileservice.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Доменная модель File — не содержит JPA/фреймворк-аннотаций.
 */
public class File {
    private UUID id;
    private String originalName;
    private String storageKey;
    private String mimeType;
    private Long size;
    private String extension;
    private LocalDateTime uploadDate;
    private UUID uploaderId;
    private String bucketName = "files";
    private String description;
    private Set<UUID> applicationIds = new HashSet<>();

    public File() {}

    private File(UUID id, String originalName, Long size, String mimeType, UUID uploaderId) {
        this.id = id;
        this.originalName = originalName;
        this.size = size;
        this.mimeType = mimeType;
        this.uploaderId = uploaderId;
        this.uploadDate = LocalDateTime.now();
        this.extension = extractExtension(originalName);
        this.storageKey = generateStorageKey(id, extension);
    }

    public static File createNew(String originalName, Long size, String mimeType, UUID uploaderId) {
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Original name required");
        }
        UUID id = UUID.randomUUID();
        return new File(id, originalName.trim(), size, mimeType, uploaderId);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private String generateStorageKey(UUID fileId, String extension) {
        if (extension == null || extension.isEmpty()) return fileId.toString();
        return fileId.toString() + "." + extension;
    }

    // getters/setters

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

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<UUID> getApplicationIds() { return applicationIds; }
    public void setApplicationIds(Set<UUID> applicationIds) { this.applicationIds = applicationIds == null ? new HashSet<>() : new HashSet<>(applicationIds); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof File)) return false;
        File file = (File) o;
        return Objects.equals(id, file.id) && Objects.equals(storageKey, file.storageKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, storageKey);
    }
}
