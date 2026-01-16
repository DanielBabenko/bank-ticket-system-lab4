package com.example.fileservice.controller;

import com.example.fileservice.dto.FileDto;
import com.example.fileservice.model.entity.File;
import com.example.fileservice.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Files", description = "API for managing files")
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private static final int MAX_PAGE_SIZE = 50;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "Upload a file", description = "Uploads a file to storage and saves metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FileDto> uploadFile(
            @Parameter(description = "File to upload")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "File description (optional)")
            @RequestParam(value = "description", required = false) String description,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            UriComponentsBuilder uriBuilder) {

        // Проверка размера файла
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File size cannot exceed %d bytes", MAX_FILE_SIZE));
        }

        // Получаем информацию о пользователе из JWT
        String userIdStr = jwt.getClaimAsString("uid");
        String username = jwt.getClaimAsString("preferred_username");

        if (userIdStr == null) {
            throw new IllegalArgumentException("User ID not found in token");
        }

        UUID userId = UUID.fromString(userIdStr);

        // Загружаем файл
        FileDto fileDto = fileService.uploadFile(file, userId, username, description);

        URI location = uriBuilder.path("/api/v1/files/{id}")
                .buildAndExpand(fileDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(fileDto);
    }

    @Operation(summary = "Download a file", description = "Downloads a file by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "File ID")
            @PathVariable UUID id) {

        FileService.InputStreamWithMetadata fileData = fileService.downloadFile(id);

        // Настраиваем заголовки ответа
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(fileData.filename())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.parseMediaType(fileData.contentType()));
        headers.setContentLength(fileData.size());

        InputStreamResource resource = new InputStreamResource(fileData.inputStream());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @Operation(summary = "List all files", description = "Returns list of files with metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of files"),
            @ApiResponse(responseCode = "400", description = "Page size too large")
    })
    @GetMapping
    public ResponseEntity<List<FileDto>> listFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Page size cannot be greater than %d", MAX_PAGE_SIZE));
        }

        Page<FileDto> filePage = fileService.listAll(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(filePage.getTotalElements()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(filePage.getContent());
    }

    @Operation(summary = "Get file metadata", description = "Returns metadata about a file including associated applications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File metadata retrieved"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFile(@PathVariable UUID id) {
        FileDto response = fileService.getFileById(id);
        log.info("Returning file {} with {} applications", id, response.getApplications().size());
        return ResponseEntity.ok(response);
    }

    // internal-запрос для application-service
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<FileDto>> getFilesBatch(
            @Valid @RequestBody List<UUID> fileIds) {

        if (fileIds == null || fileIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<FileDto> dtos = fileService.getFilesBatch(fileIds);

        log.info("Processed batch of {} files", dtos.size());
        return ResponseEntity.ok(dtos);
    }
}
