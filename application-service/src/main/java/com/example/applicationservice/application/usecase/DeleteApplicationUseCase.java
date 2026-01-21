package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.port.inbound.DeleteApplicationUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

import java.util.UUID;

/**
 * Delete single application (admin only).
 */
public class DeleteApplicationUseCase implements DeleteApplicationUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final ApplicationHistoryRepositoryPort historyRepository;

    public DeleteApplicationUseCase(ApplicationRepositoryPort applicationRepository, ApplicationHistoryRepositoryPort historyRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void deleteApplication(UUID applicationId, UUID actorId, String actorRoleClaim) {
        boolean isAdmin = "ROLE_ADMIN".equals(actorRoleClaim);
        if (!isAdmin) throw new ForbiddenException("Only admin can delete applications");

        applicationRepository.deleteFilesByApplicationId(applicationId);
        historyRepository.deleteByApplicationId(applicationId);
        applicationRepository.deleteTagsByApplicationId(applicationId);
        applicationRepository.deleteById(applicationId);
    }
}
