package com.example.fileservice.controller;

import com.example.fileservice.dto.AttachFileRequest;
import com.example.fileservice.dto.FileMetadataDto;
import com.example.fileservice.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileMetadataDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(value = "applicationId", required = false) UUID applicationId) {

        FileMetadataDto metadata = fileService.uploadFile(file, userId, applicationId);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable UUID fileId,
            @RequestHeader("X-User-Id") UUID userId) {

        Resource resource = fileService.loadFileAsResource(fileId, userId);
        FileMetadataDto metadata = fileService.getFileMetadata(fileId, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{fileId}/metadata")
    public ResponseEntity<FileMetadataDto> getFileMetadata(
            @PathVariable UUID fileId,
            @RequestHeader("X-User-Id") UUID userId) {

        FileMetadataDto metadata = fileService.getFileMetadata(fileId, userId);
        return ResponseEntity.ok(metadata);
    }

    @PostMapping("/{fileId}/attach")
    public ResponseEntity<Void> attachToApplication(
            @PathVariable UUID fileId,
            @RequestBody AttachFileRequest request,
            @RequestHeader("X-User-Id") UUID userId) {

        fileService.attachToApplication(fileId, request.getApplicationId(), userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<FileMetadataDto>> getFilesByApplication(
            @PathVariable UUID applicationId) {

        List<FileMetadataDto> files = fileService.getFilesByApplication(applicationId);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID fileId,
            @RequestHeader("X-User-Id") UUID userId) {

        fileService.deleteFile(fileId, userId);
        return ResponseEntity.noContent().build();
    }
}