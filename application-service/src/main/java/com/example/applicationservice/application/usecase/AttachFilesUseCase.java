package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.event.FileEvent;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.AttachFilesUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.EventPublisherPort;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Attach files — adds file ids to application and publishes file attach event.
 */
public class AttachFilesUseCase implements AttachFilesUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final EventPublisherPort eventPublisher;

    public AttachFilesUseCase(ApplicationRepositoryPort applicationRepository, EventPublisherPort eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void attachFiles(UUID applicationId, List<UUID> fileIds, UUID actorId, String actorRoleClaim) {
        Application app = applicationRepository.findByIdWithFiles(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        boolean allowed = app.getApplicantId().equals(actorId) || "ROLE_ADMIN".equals(actorRoleClaim) || "ROLE_MANAGER".equals(actorRoleClaim);
        if (!allowed) throw new ForbiddenException("Insufficient permissions");

        if (app.getFiles() == null) app.setFiles(new HashSet<>());
        app.getFiles().addAll(fileIds);
        applicationRepository.save(app);

        try {
            FileEvent fe = new FileEvent(UUID.randomUUID(), "FILE_ATTACH_REQUEST", applicationId, actorId, fileIds);
            eventPublisher.publishFileAttachRequest(fe);
        } catch (Exception e) {
            // ignore
        }
    }
}
