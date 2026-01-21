package com.example.fileservice.application.command;

import java.io.InputStream;
import java.util.UUID;

public class UploadFileCommand {
    private final InputStream inputStream;
    private final String originalName;
    private final long size;
    private final String contentType;
    private final UUID uploaderId;
    private final String description;

    public UploadFileCommand(InputStream inputStream, String originalName, long size, String contentType, UUID uploaderId, String description) {
        this.inputStream = inputStream;
        this.originalName = originalName;
        this.size = size;
        this.contentType = contentType;
        this.uploaderId = uploaderId;
        this.description = description;
    }

    public InputStream getInputStream() { return inputStream; }
    public String getOriginalName() { return originalName; }
    public long getSize() { return size; }
    public String getContentType() { return contentType; }
    public UUID getUploaderId() { return uploaderId; }
    public String getDescription() { return description; }
}
