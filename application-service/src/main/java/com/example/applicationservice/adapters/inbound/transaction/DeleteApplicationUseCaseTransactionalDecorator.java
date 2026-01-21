package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.DeleteApplicationUseCase;
import com.example.applicationservice.domain.port.inbound.DeleteApplicationUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteApplicationUseCaseTransactionalDecorator implements DeleteApplicationUseCasePort {

    private final DeleteApplicationUseCase delegate;

    public DeleteApplicationUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                          ApplicationHistoryRepositoryPort historyRepositoryPort) {
        this.delegate = new DeleteApplicationUseCase(applicationRepositoryPort, historyRepositoryPort);
    }

    @Override
    public void deleteApplication(UUID applicationId, UUID actorId, String actorRoleClaim) {
        delegate.deleteApplication(applicationId, actorId, actorRoleClaim);
    }
}
