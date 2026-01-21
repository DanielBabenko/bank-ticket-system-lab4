package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.ForbiddenException;
import com.example.applicationservice.application.exception.NotFoundException;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListHistoryUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private ApplicationHistoryRepositoryPort historyRepository;
    private ListHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        historyRepository = mock(ApplicationHistoryRepositoryPort.class);
        useCase = new ListHistoryUseCase(applicationRepository, historyRepository);
    }

    @Test
    void listHistory_applicationNotFound_throwsNotFoundException() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findById(appId)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () ->
                useCase.listHistory(appId, UUID.randomUUID(), "ROLE_USER"));
    }

    @Test
    void listHistory_insufficientPermissions_throwsForbiddenException() {
        UUID appId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(applicantId);

        when(applicationRepository.findById(appId)).thenReturn(java.util.Optional.of(app));

        UUID actorId = UUID.randomUUID(); // не заявитель
        assertThrows(ForbiddenException.class, () ->
                useCase.listHistory(appId, actorId, "ROLE_USER"));
    }

    @Test
    void listHistory_success_returnsHistory_forApplicant() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(actorId);

        ApplicationHistory hist1 = new ApplicationHistory();
        ApplicationHistory hist2 = new ApplicationHistory();

        when(applicationRepository.findById(appId)).thenReturn(java.util.Optional.of(app));
        when(historyRepository.findByApplicationIdOrderByChangedAtDesc(appId))
                .thenReturn(List.of(hist1, hist2));

        List<ApplicationHistory> result = useCase.listHistory(appId, actorId, "ROLE_USER");

        assertEquals(2, result.size());
        verify(historyRepository).findByApplicationIdOrderByChangedAtDesc(appId);
    }

    @Test
    void listHistory_success_returnsHistory_forAdminOrManager() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());

        ApplicationHistory hist = new ApplicationHistory();

        when(applicationRepository.findById(appId)).thenReturn(java.util.Optional.of(app));
        when(historyRepository.findByApplicationIdOrderByChangedAtDesc(appId))
                .thenReturn(List.of(hist));

        // Admin
        List<ApplicationHistory> resultAdmin = useCase.listHistory(appId, actorId, "ROLE_ADMIN");
        assertEquals(1, resultAdmin.size());

        // Manager
        List<ApplicationHistory> resultManager = useCase.listHistory(appId, actorId, "ROLE_MANAGER");
        assertEquals(1, resultManager.size());
    }
}
