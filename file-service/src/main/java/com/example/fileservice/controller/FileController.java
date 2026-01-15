package com.example.fileservice.controller;

import com.example.fileservice.dto.AttachFilesRequest;
import com.example.fileservice.dto.FileMetadataResponse;
import com.example.fileservice.dto.FileUploadResponse;
import com.example.fileservice.service.FileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = extractUserId(jwt);
        FileUploadResponse response = fileService.uploadFile(file, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileId}/metadata")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(@PathVariable UUID fileId) {
        FileMetadataResponse response = fileService.getFileMetadata(fileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
        byte[] fileContent = fileService.downloadFile(fileId);
        FileMetadataResponse metadata = fileService.getFileMetadata(fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
        headers.setContentDispositionFormData("attachment", metadata.getFileName());
        headers.setContentLength(fileContent.length);

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    @PostMapping("/attach-to-application")
    public ResponseEntity<Void> attachFilesToApplication(
            @Valid @RequestBody AttachFilesRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = extractUserId(jwt);
        // Можно добавить проверку прав доступа

        fileService.attachFilesToApplication(request.getApplicationId(), request.getFileIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<FileMetadataResponse>> getApplicationFiles(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = extractUserId(jwt);
        // Можно добавить проверку прав доступа

        List<FileMetadataResponse> files = fileService.getFilesByApplication(applicationId);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = extractUserId(jwt);
        // Проверка, что пользователь является владельцем файла или админом

        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Jwt jwt) {
        String userIdClaim = jwt.getClaimAsString("sub");
        if (userIdClaim == null) {
            userIdClaim = jwt.getClaimAsString("userId");
        }
        return UUID.fromString(userIdClaim);
    }
}