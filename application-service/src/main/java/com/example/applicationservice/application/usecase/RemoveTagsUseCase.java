package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.RemoveTagsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

import java.util.List;
import java.util.UUID;

/**
 * Remove tags.
 */
public class RemoveTagsUseCase implements RemoveTagsUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;

    public RemoveTagsUseCase(ApplicationRepositoryPort applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public void removeTags(UUID applicationId, List<String> tagNames, UUID actorId, String actorRoleClaim) {
        Application app = applicationRepository.findByIdWithTags(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        boolean allowed = app.getApplicantId().equals(actorId) || "ROLE_ADMIN".equals(actorRoleClaim) || "ROLE_MANAGER".equals(actorRoleClaim);
        if (!allowed) throw new ForbiddenException("Insufficient permissions");

        if (app.getTags() != null) {
            tagNames.forEach(n -> app.getTags().remove(n));
            applicationRepository.save(app);
        }
    }
}
