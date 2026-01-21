package com.example.applicationservice.adapters.inbound.transaction;

import com.example.applicationservice.application.usecase.ListHistoryUseCase;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.port.inbound.ListHistoryUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListHistoryUseCaseTransactionalDecorator implements ListHistoryUseCasePort {

    private final ListHistoryUseCase delegate;

    public ListHistoryUseCaseTransactionalDecorator(ApplicationRepositoryPort applicationRepositoryPort,
                                                    ApplicationHistoryRepositoryPort historyRepositoryPort) {
        this.delegate = new ListHistoryUseCase(applicationRepositoryPort, historyRepositoryPort);
    }

    @Override
    public List<ApplicationHistory> listHistory(UUID applicationId, UUID actorId, String actorRoleClaim) {
        return delegate.listHistory(applicationId, actorId, actorRoleClaim);
    }
}
