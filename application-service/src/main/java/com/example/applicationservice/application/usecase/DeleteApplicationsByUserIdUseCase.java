package com.example.applicationservice.application.usecase;

import com.example.applicationservice.domain.port.inbound.DeleteApplicationsByUserIdUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

import java.util.List;
import java.util.UUID;

/**
 * Delete all applications by user id.
 */
public class DeleteApplicationsByUserIdUseCase implements DeleteApplicationsByUserIdUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final ApplicationHistoryRepositoryPort historyRepository;

    public DeleteApplicationsByUserIdUseCase(ApplicationRepositoryPort applicationRepository, ApplicationHistoryRepositoryPort historyRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void deleteApplicationsByUserId(UUID userId) {
        List<UUID> appIds = applicationRepository.findIdsByApplicantId(userId);
        for (UUID appId : appIds) {
            applicationRepository.deleteFilesByApplicationId(appId);
            historyRepository.deleteByApplicationId(appId);
            applicationRepository.deleteTagsByApplicationId(appId);
            applicationRepository.deleteById(appId);
        }
    }
}
