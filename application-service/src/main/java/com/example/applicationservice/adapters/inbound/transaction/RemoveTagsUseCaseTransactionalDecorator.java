package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.RemoveTagsUseCase;
import com.example.applicationservice.domain.port.inbound.RemoveTagsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RemoveTagsUseCaseTransactionalDecorator implements RemoveTagsUseCasePort {

    private final RemoveTagsUseCase delegate;

    public RemoveTagsUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort) {
        this.delegate = new RemoveTagsUseCase(applicationRepositoryPort);
    }

    @Override
    public void removeTags(UUID applicationId, List<String> tagNames, UUID actorId, String actorRoleClaim) {
        delegate.removeTags(applicationId, tagNames, actorId, actorRoleClaim);
    }
}
