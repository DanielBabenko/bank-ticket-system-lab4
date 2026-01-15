package com.example.applicationservice.dto;

import jakarta.validation.constraints.NotBlank;

public class DocumentRequest {
    @NotBlank
    private String fileName;
    private String contentType;
    private String fileId;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
}