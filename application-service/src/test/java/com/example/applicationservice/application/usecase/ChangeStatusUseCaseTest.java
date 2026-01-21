package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.model.enums.ApplicationStatus;
import com.example.applicationservice.domain.model.enums.UserRole;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangeStatusUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private ApplicationHistoryRepositoryPort historyRepository;
    private ChangeStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        historyRepository = mock(ApplicationHistoryRepositoryPort.class);
        useCase = new ChangeStatusUseCase(applicationRepository, historyRepository);
    }

    @Test
    void changeStatus_unauthenticated_throwsUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () ->
                useCase.changeStatus(UUID.randomUUID(), "APPROVED", null, "ROLE_ADMIN"));
    }

    @Test
    void changeStatus_userNotAdminOrManager_throwsForbiddenException() {
        UUID actorId = UUID.randomUUID();
        assertThrows(ForbiddenException.class, () ->
                useCase.changeStatus(UUID.randomUUID(), "APPROVED", actorId, "ROLE_USER"));
    }

    @Test
    void changeStatus_applicationNotFound_throwsNotFoundException() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                useCase.changeStatus(appId, "APPROVED", actorId, "ROLE_ADMIN"));
    }

    @Test
    void changeStatus_managerCannotChangeOwnApplication_throwsConflictException() {
        UUID actorId = UUID.randomUUID();
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setApplicantId(actorId);

        when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));

        assertThrows(ConflictException.class, () ->
                useCase.changeStatus(app.getId(), "APPROVED", actorId, "ROLE_MANAGER"));
    }

    @Test
    void changeStatus_invalidStatus_throwsConflictException() {
        UUID actorId = UUID.randomUUID();
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setApplicantId(UUID.randomUUID());

        when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));

        assertThrows(ConflictException.class, () ->
                useCase.changeStatus(app.getId(), "INVALID_STATUS", actorId, "ROLE_ADMIN"));
    }

    @Test
    void changeStatus_successfulChange_savesApplicationAndHistory() {
        UUID actorId = UUID.randomUUID();
        UUID appId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setStatus(ApplicationStatus.DRAFT);
        app.setUpdatedAt(Instant.now());

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));
        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(Optional.of(app));
        when(applicationRepository.findByIdWithTags(appId)).thenReturn(Optional.of(app));

        Application result = useCase.changeStatus(appId, "SUBMITTED", actorId, "ROLE_ADMIN");

        // Проверка нового статуса
        assertEquals(ApplicationStatus.SUBMITTED, result.getStatus());
        verify(applicationRepository).save(app);

        // Проверка истории
        ArgumentCaptor<ApplicationHistory> captor = ArgumentCaptor.forClass(ApplicationHistory.class);
        verify(historyRepository).save(captor.capture());
        ApplicationHistory hist = captor.getValue();
        assertEquals(appId, hist.getApplicationId());
        assertEquals(ApplicationStatus.DRAFT, hist.getOldStatus());
        assertEquals(ApplicationStatus.SUBMITTED, hist.getNewStatus());
        assertEquals(UserRole.ROLE_ADMIN, hist.getChangedBy());
    }

    @Test
    void changeStatus_sameStatus_doesNotCreateHistory() {
        UUID actorId = UUID.randomUUID();
        UUID appId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setStatus(ApplicationStatus.DRAFT);

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        Application result = useCase.changeStatus(appId, "DRAFT", actorId, "ROLE_ADMIN");

        // Статус остался прежним
        assertEquals(ApplicationStatus.DRAFT, result.getStatus());
        verify(applicationRepository, never()).save(app);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void changeStatus_trimStatusString_worksCorrectly() {
        UUID actorId = UUID.randomUUID();
        UUID appId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setStatus(ApplicationStatus.DRAFT);

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        Application result = useCase.changeStatus(appId, " submitted ", actorId, "ROLE_ADMIN");

        assertEquals(ApplicationStatus.SUBMITTED, result.getStatus());
        verify(applicationRepository).save(app);
        verify(historyRepository).save(any());
    }
}
