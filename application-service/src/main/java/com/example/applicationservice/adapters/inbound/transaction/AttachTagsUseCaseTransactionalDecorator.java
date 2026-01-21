package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.AttachTagsUseCase;
import com.example.applicationservice.domain.port.inbound.AttachTagsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.EventPublisherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AttachTagsUseCaseTransactionalDecorator implements AttachTagsUseCasePort {

    private final AttachTagsUseCase delegate;

    public AttachTagsUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                   EventPublisherPort eventPublisherPort) {
        this.delegate = new AttachTagsUseCase(applicationRepositoryPort, eventPublisherPort);
    }

    @Override
    public void attachTags(UUID applicationId, List<String> tagNames, UUID actorId, String actorRoleClaim) {
        delegate.attachTags(applicationId, tagNames, actorId, actorRoleClaim);
    }
}
