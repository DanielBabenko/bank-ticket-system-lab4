package com.example.applicationservice.application.usecase;

import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class DeleteApplicationsByUserIdUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private ApplicationHistoryRepositoryPort historyRepository;
    private DeleteApplicationsByUserIdUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        historyRepository = mock(ApplicationHistoryRepositoryPort.class);
        useCase = new DeleteApplicationsByUserIdUseCase(applicationRepository, historyRepository);
    }

    @Test
    void deleteApplicationsByUserId_deletesAllApplications() {
        UUID userId = UUID.randomUUID();
        UUID app1 = UUID.randomUUID();
        UUID app2 = UUID.randomUUID();

        when(applicationRepository.findIdsByApplicantId(userId)).thenReturn(List.of(app1, app2));

        useCase.deleteApplicationsByUserId(userId);

        // Проверяем, что все методы удаления вызваны для каждой заявки
        for (UUID appId : List.of(app1, app2)) {
            verify(applicationRepository).deleteFilesByApplicationId(appId);
            verify(historyRepository).deleteByApplicationId(appId);
            verify(applicationRepository).deleteTagsByApplicationId(appId);
            verify(applicationRepository).deleteById(appId);
        }
    }

    @Test
    void deleteApplicationsByUserId_noApplications_nothingDeleted() {
        UUID userId = UUID.randomUUID();
        when(applicationRepository.findIdsByApplicantId(userId)).thenReturn(List.of());

        useCase.deleteApplicationsByUserId(userId);

        // Проверяем, что ни один метод удаления не был вызван
        verify(applicationRepository, never()).deleteFilesByApplicationId(any());
        verify(historyRepository, never()).deleteByApplicationId(any());
        verify(applicationRepository, never()).deleteTagsByApplicationId(any());
        verify(applicationRepository, never()).deleteById(any());
    }
}
