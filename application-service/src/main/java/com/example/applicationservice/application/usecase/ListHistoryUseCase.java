package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.ForbiddenException;
import com.example.applicationservice.application.exception.NotFoundException;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.port.inbound.ListHistoryUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

import java.util.List;
import java.util.UUID;

/**
 * Returns history for a given application after validating actor's permissions.
 */
public class ListHistoryUseCase implements ListHistoryUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final ApplicationHistoryRepositoryPort historyRepository;

    public ListHistoryUseCase(ApplicationRepositoryPort applicationRepository, ApplicationHistoryRepositoryPort historyRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public List<ApplicationHistory> listHistory(UUID applicationId, UUID actorId, String actorRoleClaim) {
        Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new NotFoundException("Application not found"));
        boolean canView = app.getApplicantId().equals(actorId) || "ROLE_ADMIN".equals(actorRoleClaim) || "ROLE_MANAGER".equals(actorRoleClaim);
        if (!canView) throw new ForbiddenException("Insufficient permissions to view history");
        return historyRepository.findByApplicationIdOrderByChangedAtDesc(applicationId);
    }
}
