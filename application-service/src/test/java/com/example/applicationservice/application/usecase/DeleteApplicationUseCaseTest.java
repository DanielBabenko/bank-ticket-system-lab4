package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.ForbiddenException;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DeleteApplicationUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private ApplicationHistoryRepositoryPort historyRepository;
    private DeleteApplicationUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        historyRepository = mock(ApplicationHistoryRepositoryPort.class);
        useCase = new DeleteApplicationUseCase(applicationRepository, historyRepository);
    }

    @Test
    void deleteApplication_notAdmin_throwsForbiddenException() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        assertThrows(ForbiddenException.class, () ->
                useCase.deleteApplication(appId, actorId, "ROLE_USER"));
    }

    @Test
    void deleteApplication_admin_callsAllDeleteMethods() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        useCase.deleteApplication(appId, actorId, "ROLE_ADMIN");

        verify(applicationRepository).deleteFilesByApplicationId(appId);
        verify(historyRepository).deleteByApplicationId(appId);
        verify(applicationRepository).deleteTagsByApplicationId(appId);
        verify(applicationRepository).deleteById(appId);
    }
}
