package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.event.TagEvent;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.AttachTagsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.EventPublisherPort;

import java.util.*;

/**
 * Attach tags (synchronous).
 */
public class AttachTagsUseCase implements AttachTagsUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final EventPublisherPort eventPublisher;

    public AttachTagsUseCase(ApplicationRepositoryPort applicationRepository, EventPublisherPort eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void attachTags(UUID applicationId, List<String> tagNames, UUID actorId, String actorRoleClaim) {
        // validate actor: allowed if applicant or admin/manager
        Application app = applicationRepository.findByIdWithTags(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        boolean allowed = app.getApplicantId().equals(actorId) || "ROLE_ADMIN".equals(actorRoleClaim) || "ROLE_MANAGER".equals(actorRoleClaim);
        if (!allowed) throw new ForbiddenException("Insufficient permissions");

        if (app.getTags() == null) app.setTags(new HashSet<>());
        app.getTags().addAll(tagNames);
        applicationRepository.save(app);

        try {
            TagEvent ev = new TagEvent(UUID.randomUUID(), "TAG_ATTACH_REQUEST", applicationId, actorId, tagNames);
            eventPublisher.publishTagAttachRequest(ev);
        } catch (Exception e) {
            // ignore publisher errors at this layer
        }
    }
}
