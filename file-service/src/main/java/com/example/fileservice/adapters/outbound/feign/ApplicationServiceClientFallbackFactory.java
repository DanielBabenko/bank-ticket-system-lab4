package com.example.fileservice.adapters.outbound.feign;

import com.example.fileservice.application.dto.ApplicationInfoDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ApplicationServiceClientFallbackFactory implements FallbackFactory<ApplicationServiceClientFeign> {
    @Override
    public ApplicationServiceClientFeign create(Throwable cause) {
        return new ApplicationServiceClientFeign() {
            @Override
            public List<ApplicationInfoDto> getApplicationsByFile(UUID fileId) {
                return List.of(); // возвращаем пустой список при недоступности
            }
        };
    }
}
