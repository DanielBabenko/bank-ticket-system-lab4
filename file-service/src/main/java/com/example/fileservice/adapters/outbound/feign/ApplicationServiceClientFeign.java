package com.example.fileservice.adapters.outbound.feign;

import com.example.fileservice.application.dto.ApplicationInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "application-service",
        fallbackFactory = ApplicationServiceClientFallbackFactory.class
)
public interface ApplicationServiceClientFeign {

    @GetMapping("/api/v1/applications/by-file")
    List<ApplicationInfoDto> getApplicationsByFile(@RequestParam("file") UUID fileId);
}
