package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.RemoveFilesUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

import java.util.List;
import java.util.UUID;

/**
 * Remove files from application.
 */
public class RemoveFilesUseCase implements RemoveFilesUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;

    public RemoveFilesUseCase(ApplicationRepositoryPort applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public void removeFiles(UUID applicationId, List<UUID> fileIds, UUID actorId, String actorRoleClaim) {
        Application app = applicationRepository.findByIdWithFiles(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        boolean allowed = app.getApplicantId().equals(actorId) || "ROLE_ADMIN".equals(actorRoleClaim) || "ROLE_MANAGER".equals(actorRoleClaim);
        if (!allowed) throw new ForbiddenException("Insufficient permissions");

        if (app.getFiles() != null) fileIds.forEach(app.getFiles()::remove);
        applicationRepository.save(app);
    }
}
