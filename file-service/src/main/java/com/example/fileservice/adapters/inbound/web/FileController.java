package com.example.fileservice.adapters.inbound.web;

import com.example.fileservice.application.command.UploadFileCommand;
import com.example.fileservice.application.dto.FileDto;
import com.example.fileservice.application.mapper.FileMapper;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.inbound.*;
import com.example.fileservice.domain.port.outbound.ApplicationServicePort;
import io.swagger.v3.oas.annotations.Operation;
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

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Files", description = "API for managing files")
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private static final int MAX_PAGE_SIZE = 50;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final UploadFileUseCasePort uploadUseCase;
    private final DownloadFileUseCasePort downloadUseCase;
    private final DeleteFileUseCasePort deleteUseCase;
    private final ListFilesUseCasePort listUseCase;
    private final GetFileUseCasePort getFileUseCase;
    private final GetFilesBatchUseCasePort getFilesBatchUseCase;
    private final ApplicationServicePort applicationServicePort;

    public FileController(UploadFileUseCasePort uploadUseCase,
                          DownloadFileUseCasePort downloadUseCase,
                          DeleteFileUseCasePort deleteUseCase,
                          ListFilesUseCasePort listUseCase,
                          GetFileUseCasePort getFileUseCase,
                          GetFilesBatchUseCasePort getFilesBatchUseCase,
                          ApplicationServicePort applicationServicePort) {
        this.uploadUseCase = uploadUseCase;
        this.downloadUseCase = downloadUseCase;
        this.deleteUseCase = deleteUseCase;
        this.listUseCase = listUseCase;
        this.getFileUseCase = getFileUseCase;
        this.getFilesBatchUseCase = getFilesBatchUseCase;
        this.applicationServicePort = applicationServicePort;
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
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriBuilder) throws Exception {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(String.format("File size cannot exceed %d bytes", MAX_FILE_SIZE));
        }

        String userIdStr = jwt.getClaimAsString("uid");
        if (userIdStr == null) throw new IllegalArgumentException("User ID not found in token");
        UUID userId = UUID.fromString(userIdStr);

        var command = new UploadFileCommand(file.getInputStream(), file.getOriginalFilename(), file.getSize(), file.getContentType(), userId, description);
        File saved = uploadUseCase.uploadFile(command);

        // Получаем URL через ApplicationServicePort? Нет — используем storage adapter отдельно.
        String downloadUrl = applicationServicePort == null ? null : null; // adapter will be used in mapping below if needed

        FileDto dto = FileMapper.toDto(saved, List.of(), null); // заполнение applications и downloadUrl ниже

        URI location = uriBuilder.path("/api/v1/files/{id}").buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @Operation(summary = "Download a file", description = "Downloads a file by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "You must be uploader, manager or admin to download this file"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) throws Exception {
        String userIdStr = jwt.getClaimAsString("uid");
        if (userIdStr == null) throw new IllegalArgumentException("User ID not found in token");
        UUID userId = UUID.fromString(userIdStr);

        var res = downloadUseCase.downloadFile(id, userId, jwt);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(res.filename)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.parseMediaType(res.contentType));
        headers.setContentLength(res.size);

        InputStreamResource resource = new InputStreamResource(res.inputStream);

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @Operation(
            summary = "Delete a file",
            description = "Deletes a file by its ID. Only file owner or admin can delete files."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "You must be uploader or admin to delete this file"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String userIdStr = jwt.getClaimAsString("uid");
        if (userIdStr == null) throw new IllegalArgumentException("User ID not found in token");
        UUID userId = UUID.fromString(userIdStr);

        deleteUseCase.deleteFile(id, userId, jwt);
        log.info("File deleted: {} by user {}", id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List all files", description = "Returns list of files with metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of files"),
            @ApiResponse(responseCode = "400", description = "Page size too large")
    })
    @GetMapping
    public ResponseEntity<List<FileDto>> listFiles(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {

        if (size > MAX_PAGE_SIZE) throw new IllegalArgumentException("Page size too large");

        List<com.example.fileservice.domain.model.File> files = listUseCase.listAll(page, size);

        List<FileDto> dtos = files.stream()
                .map(f -> FileMapper.toDto(f, List.of(), null))
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(dtos.size()));

        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    @Operation(summary = "Get file metadata", description = "Returns metadata about a file including associated applications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File metadata retrieved"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFile(@PathVariable UUID id) {
        com.example.fileservice.domain.model.File file = getFileUseCase.getFileById(id);

        // Получаем applications через ApplicationServicePort (адаптер)
        var apps = applicationServicePort.getApplicationsByFile(file.getId());
        String downloadUrl = null; // storage adapter можно инжектить и получить URL, но чтобы не дублировать — опустил

        FileDto dto = FileMapper.toDto(file, apps, downloadUrl);
        return ResponseEntity.ok(dto);
    }

    // internal-запрос для application-service
    @PostMapping("/check")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<UUID>> getRealFileIds(@Valid @RequestBody List<UUID> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) return ResponseEntity.ok(List.of());
        List<UUID> realIds = getFilesBatchUseCase.getFilesBatch(fileIds);
        return ResponseEntity.ok(realIds);
    }
}
