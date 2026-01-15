package com.example.applicationservice.feign;

import com.example.applicationservice.dto.AttachFileRequest;
import com.example.applicationservice.dto.FileMetadataDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "file-service",
        fallbackFactory = FileServiceClientFallbackFactory.class)
public interface FileServiceClient {

    @GetMapping("/api/v1/files/application/{applicationId}")
    @CircuitBreaker(name = "file-service")
    List<FileMetadataDto> getFilesByApplication(@PathVariable UUID applicationId);

    @GetMapping("/api/v1/files/{fileId}/metadata")
    @CircuitBreaker(name = "file-service")
    FileMetadataDto getFileMetadata(@PathVariable UUID fileId);

    @PostMapping("/api/v1/files/{fileId}/attach")
    void attachFileToApplication(@PathVariable UUID fileId,
                                 @RequestBody AttachFileRequest request);
}