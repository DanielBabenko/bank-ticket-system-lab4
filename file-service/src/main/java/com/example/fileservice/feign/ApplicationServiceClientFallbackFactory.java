package com.example.fileservice.feign;

import com.example.fileservice.dto.ApplicationInfoDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Component
public class ApplicationServiceClientFallbackFactory implements FallbackFactory<ApplicationServiceClient> {
    @Override
    public ApplicationServiceClient create(Throwable cause) {
        return new ApplicationServiceClient() {
            @Override
            public List<ApplicationInfoDto> getApplicationsByFile(@RequestParam("file") UUID fileId) {
                return null;
            }
        };
    }
}