package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.RemoveFilesUseCase;
import com.example.applicationservice.domain.port.inbound.RemoveFilesUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RemoveFilesUseCaseTransactionalDecorator implements RemoveFilesUseCasePort {

    private final RemoveFilesUseCase delegate;

    public RemoveFilesUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort) {
        this.delegate = new RemoveFilesUseCase(applicationRepositoryPort);
    }

    @Override
    public void removeFiles(UUID applicationId, List<UUID> fileIds, UUID actorId, String actorRoleClaim) {
        delegate.removeFiles(applicationId, fileIds, actorId, actorRoleClaim);
    }
}
