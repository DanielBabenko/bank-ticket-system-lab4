package com.example.tagservice.adapters.outbound.feign;

import com.example.tagservice.application.dto.ApplicationInfoDto;
import com.example.tagservice.domain.dto.ApplicationInfo;
import com.example.tagservice.domain.port.outbound.ApplicationServicePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Адаптер реализует доменный порт ApplicationServicePort, использует Feign client,
 * и маппит application DTO в доменные DTO.
 */
@Component
public class ApplicationServiceAdapter implements ApplicationServicePort {

    private final ApplicationServiceClientFeign feignClient;

    public ApplicationServiceAdapter(ApplicationServiceClientFeign feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public List<ApplicationInfo> getApplicationsByTag(String tagName) {
        List<ApplicationInfoDto> dtos = feignClient.getApplicationsByTag(tagName);
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
