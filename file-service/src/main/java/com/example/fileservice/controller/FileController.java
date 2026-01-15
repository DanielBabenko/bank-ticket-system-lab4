package com.example.fileservice.controller;

import com.example.fileservice.dto.AttachFilesRequest;
import com.example.fileservice.dto.FileMetadataResponse;
import com.example.fileservice.dto.FileUploadResponse;
import com.example.fileservice.exception.UnauthorizedException;
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
import java.util.Map;
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
        // TODO: Проверить права доступа пользователя к заявке
        // Для этого может потребоваться интеграция с application-service

        fileService.attachFilesToApplication(request.getApplicationId(), request.getFileIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<FileMetadataResponse>> getApplicationFiles(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = extractUserId(jwt);
        // TODO: Проверить права доступа пользователя к заявке

        List<FileMetadataResponse> files = fileService.getFilesByApplication(applicationId);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = extractUserId(jwt);
        boolean isAdmin = isAdmin(jwt);

        fileService.deleteFile(fileId, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Jwt jwt) {
        String userIdClaim = jwt.getClaimAsString("sub");
        if (userIdClaim == null) {
            userIdClaim = jwt.getClaimAsString("userId");
        }
        try {
            return UUID.fromString(userIdClaim);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid user ID in token");
        }
    }

    private boolean isAdmin(Jwt jwt) {
        // Проверяем роли из JWT токена
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            return roles.contains("ROLE_ADMIN");
        }

        // Альтернативный вариант: проверка по realm_access
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                return realmRoles.contains("admin") || realmRoles.contains("ROLE_ADMIN");
            }
        }

        return false;
    }
}