package com.example.applicationservice.application.usecase;

import com.example.applicationservice.domain.port.inbound.DeleteApplicationsByProductIdUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

import java.util.List;
import java.util.UUID;

/**
 * Delete all applications by product id.
 */
public class DeleteApplicationsByProductIdUseCase implements DeleteApplicationsByProductIdUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final ApplicationHistoryRepositoryPort historyRepository;

    public DeleteApplicationsByProductIdUseCase(ApplicationRepositoryPort applicationRepository, ApplicationHistoryRepositoryPort historyRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void deleteApplicationsByProductId(UUID productId) {
        List<UUID> appIds = applicationRepository.findIdsByProductId(productId);
        for (UUID appId : appIds) {
            applicationRepository.deleteFilesByApplicationId(appId);
            historyRepository.deleteByApplicationId(appId);
            applicationRepository.deleteTagsByApplicationId(appId);
            applicationRepository.deleteById(appId);
        }
    }
}
