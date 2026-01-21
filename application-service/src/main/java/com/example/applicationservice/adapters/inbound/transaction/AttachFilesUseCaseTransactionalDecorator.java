package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.AttachFilesUseCase;
import com.example.applicationservice.domain.port.inbound.AttachFilesUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.EventPublisherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AttachFilesUseCaseTransactionalDecorator implements AttachFilesUseCasePort {

    private final AttachFilesUseCase delegate;

    public AttachFilesUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                    EventPublisherPort eventPublisherPort) {
        this.delegate = new AttachFilesUseCase(applicationRepositoryPort, eventPublisherPort);
    }

    @Override
    public void attachFiles(UUID applicationId, List<UUID> fileIds, UUID actorId, String actorRoleClaim) {
        delegate.attachFiles(applicationId, fileIds, actorId, actorRoleClaim);
    }
}
