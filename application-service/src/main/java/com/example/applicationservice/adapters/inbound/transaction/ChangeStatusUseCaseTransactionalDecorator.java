package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.ChangeStatusUseCase;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.inbound.ChangeStatusUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ChangeStatusUseCaseTransactionalDecorator implements ChangeStatusUseCasePort {

    private final ChangeStatusUseCase delegate;

    public ChangeStatusUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                     ApplicationHistoryRepositoryPort historyRepositoryPort) {
        this.delegate = new ChangeStatusUseCase(applicationRepositoryPort, historyRepositoryPort);
    }

    @Override
    public Application changeStatus(UUID applicationId, String status, UUID actorId, String actorRoleClaim) {
        return delegate.changeStatus(applicationId, status, actorId, actorRoleClaim);
    }
}
