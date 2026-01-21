package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.BadRequestException;
import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.FindApplicationsByTagUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Find applications by tag and return ApplicationInfo domain DTOs.
 */
public class FindApplicationsByTagUseCase implements FindApplicationsByTagUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;

    public FindApplicationsByTagUseCase(ApplicationRepositoryPort applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public List<ApplicationInfo> findApplicationsByTag(String tagName) {
        try {
            List<Application> apps = applicationRepository.findByTag(tagName);
            return apps.stream().map(a -> {
                ApplicationInfo ai = new ApplicationInfo();
                ai.setId(a.getId());
                ai.setApplicantId(a.getApplicantId());
                ai.setProductId(a.getProductId());
                ai.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
                ai.setCreatedAt(a.getCreatedAt());
                return ai;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new BadRequestException("Failed to get applications by tag: " + e.getMessage());
        }
    }
}
