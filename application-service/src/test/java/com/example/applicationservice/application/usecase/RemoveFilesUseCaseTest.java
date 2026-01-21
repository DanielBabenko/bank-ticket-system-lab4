package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.ForbiddenException;
import com.example.applicationservice.application.exception.NotFoundException;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoveFilesUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private RemoveFilesUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        useCase = new RemoveFilesUseCase(applicationRepository);
    }

    @Test
    void removeFiles_applicationNotFound_throwsNotFoundException() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () ->
                useCase.removeFiles(appId, List.of(UUID.randomUUID()), UUID.randomUUID(), "ROLE_USER"));
    }

    @Test
    void removeFiles_insufficientPermissions_throwsForbiddenException() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setFiles(Set.of(UUID.randomUUID()));

        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(java.util.Optional.of(app));

        assertThrows(ForbiddenException.class, () ->
                useCase.removeFiles(appId, List.of(UUID.randomUUID()), actorId, "ROLE_USER"));
    }

    @Test
    void removeFiles_success_removesSpecifiedFiles() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID file1 = UUID.randomUUID();
        UUID file2 = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(actorId);
        app.setFiles(Set.of(file1, file2));

        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(java.util.Optional.of(app));

        useCase.removeFiles(appId, List.of(file1), actorId, "ROLE_USER");

        assertEquals(Set.of(file2), app.getFiles());
        verify(applicationRepository).save(app);
    }

    @Test
    void removeFiles_filesNull_doesNotThrow() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(actorId);
        app.setFiles(null);

        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(java.util.Optional.of(app));

        assertDoesNotThrow(() -> useCase.removeFiles(appId, List.of(UUID.randomUUID()), actorId, "ROLE_USER"));
        verify(applicationRepository).save(app);
    }

    @Test
    void removeFiles_adminCanRemoveFiles() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setFiles(Set.of(fileId));

        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(java.util.Optional.of(app));

        useCase.removeFiles(appId, List.of(fileId), actorId, "ROLE_ADMIN");

        assertTrue(app.getFiles().isEmpty());
        verify(applicationRepository).save(app);
    }
}
