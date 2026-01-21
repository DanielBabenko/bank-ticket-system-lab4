package com.example.applicationservice.adapters.outbound.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "file-service",
        fallbackFactory = FileServiceClientFallbackFactory.class
)
public interface FileServiceClient {
    @PostMapping("/api/v1/files/check")
    List<UUID> checkFilesExist(@RequestBody List<UUID> fileIds);
}
