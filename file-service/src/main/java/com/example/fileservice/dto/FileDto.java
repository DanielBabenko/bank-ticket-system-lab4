package com.example.fileservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FileDto {
    private UUID id;
    private String originalName;
    private String mimeType;
    private Long size;
    private String extension;
    private LocalDateTime uploadDate;
    private UUID uploaderId;
    private String uploaderUsername;
    private String description;
    private String downloadUrl; // URL для скачивания
    private List<ApplicationInfoDto> applications;

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public List<ApplicationInfoDto> getApplications() { return applications; }
    public void setApplications(List<ApplicationInfoDto> applications) { this.applications = applications; }
}