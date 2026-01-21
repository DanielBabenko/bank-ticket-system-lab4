package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.ForbiddenException;
import com.example.applicationservice.application.exception.NotFoundException;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoveTagsUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private RemoveTagsUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        useCase = new RemoveTagsUseCase(applicationRepository);
    }

    @Test
    void removeTags_applicationNotFound_throwsNotFoundException() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findByIdWithTags(appId)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () ->
                useCase.removeTags(appId, List.of("tag1"), UUID.randomUUID(), "ROLE_USER"));
    }

    @Test
    void removeTags_insufficientPermissions_throwsForbiddenException() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setTags(Set.of("tag1"));

        when(applicationRepository.findByIdWithTags(appId)).thenReturn(java.util.Optional.of(app));

        assertThrows(ForbiddenException.class, () ->
                useCase.removeTags(appId, List.of("tag1"), actorId, "ROLE_USER"));
    }

    @Test
    void removeTags_success_removesSpecifiedTags() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(actorId);
        app.setTags(Set.of("tag1", "tag2"));

        when(applicationRepository.findByIdWithTags(appId)).thenReturn(java.util.Optional.of(app));

        useCase.removeTags(appId, List.of("tag1"), actorId, "ROLE_USER");

        assertEquals(Set.of("tag2"), app.getTags());
        verify(applicationRepository).save(app);
    }

    @Test
    void removeTags_adminCanRemoveTags() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setTags(Set.of("tag1"));

        when(applicationRepository.findByIdWithTags(appId)).thenReturn(java.util.Optional.of(app));

        useCase.removeTags(appId, List.of("tag1"), actorId, "ROLE_ADMIN");

        assertTrue(app.getTags().isEmpty());
        verify(applicationRepository).save(app);
    }

    @Test
    void removeTags_managerCanRemoveTags() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setTags(Set.of("tag1"));

        when(applicationRepository.findByIdWithTags(appId)).thenReturn(java.util.Optional.of(app));

        useCase.removeTags(appId, List.of("tag1"), actorId, "ROLE_MANAGER");

        assertTrue(app.getTags().isEmpty());
        verify(applicationRepository).save(app);
    }
}
