package com.example.fileservice.adapters.outbound.feign;

import com.example.fileservice.application.dto.ApplicationInfoDto;
import com.example.fileservice.domain.model.ApplicationInfo;
import com.example.fileservice.domain.port.outbound.ApplicationServicePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApplicationServiceAdapter implements ApplicationServicePort {

    private final ApplicationServiceClientFeign feignClient;

    public ApplicationServiceAdapter(ApplicationServiceClientFeign feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public List<ApplicationInfo> getApplicationsByFile(java.util.UUID fileId) {
        List<ApplicationInfoDto> dtos = feignClient.getApplicationsByFile(fileId);
        if (dtos == null) return List.of();
        return dtos.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private ApplicationInfo toDomain(ApplicationInfoDto dto) {
        ApplicationInfo ai = new ApplicationInfo();
        ai.setId(dto.getId());
        ai.setApplicantId(dto.getApplicantId());
        ai.setProductId(dto.getProductId());
        ai.setStatus(dto.getStatus());
        ai.setCreatedAt(dto.getCreatedAt());
        return ai;
    }
}
