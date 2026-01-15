package com.example.fileservice.controller;

import com.example.fileservice.dto.FileDto;
import com.example.fileservice.model.entity.File;
import com.example.fileservice.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "Create a new  unique file", description = "Registers a new file: name if it has not already existed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File created or found successfully")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FileDto> createFile(
            @Valid @RequestBody String name,
            UriComponentsBuilder uriBuilder) {
        name = name.trim();
        var file = fileService.createIfNotExists(name);

        FileDto dto = new FileDto();
        dto.setId(file.getId());
        dto.setName(file.getName());

        URI location = uriBuilder.path("/api/v1/files/{name}")
                .buildAndExpand(dto.getName())
                .toUri();

        log.info("File created or retrieved: {}", name);
        return ResponseEntity.created(location).body(dto);
    }

    @Operation(summary = "Read all files", description = "Returns list of files")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of applications"),
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

    @Operation(summary = "Read certain file by its id", description = "Returns data about a single file: name and list of applications that uses this file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data about a single file"),
            @ApiResponse(responseCode = "404", description = "File with this name is not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFileWithApplications(@PathVariable UUID id) {
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

        List<File> files = fileService.getFiles(fileIds);

        List<FileDto> dtos = files.stream()
                .map(file -> {
                    FileDto dto = new FileDto();
                    dto.setId(file.getId());
                    dto.setName(file.getName());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Processed batch of {} files", dtos.size());
        return ResponseEntity.ok(dtos);
    }
}
