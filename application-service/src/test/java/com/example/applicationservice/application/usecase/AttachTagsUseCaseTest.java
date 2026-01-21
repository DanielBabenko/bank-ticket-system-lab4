package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.ForbiddenException;
import com.example.applicationservice.application.exception.NotFoundException;
import com.example.applicationservice.domain.event.TagEvent;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.EventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttachTagsUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private EventPublisherPort eventPublisher;
    private AttachTagsUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        eventPublisher = mock(EventPublisherPort.class);
        useCase = new AttachTagsUseCase(applicationRepository, eventPublisher);
    }

    @Test
    void attachTags_applicationNotFound_throwsNotFoundException() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findByIdWithTags(appId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                useCase.attachTags(appId, List.of("tag1"), UUID.randomUUID(), "ROLE_USER"));
    }

    @Test
    void attachTags_insufficientPermissions_throwsForbiddenException() {
        UUID appId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        Application app = new Application();
        app.setApplicantId(applicantId);
        app.setTags(new HashSet<>());
        when(applicationRepository.findByIdWithTags(appId)).thenReturn(Optional.of(app));

        UUID actorId = UUID.randomUUID(); // не совпадает с заявителем
        assertThrows(ForbiddenException.class, () ->
                useCase.attachTags(appId, List.of("tag1"), actorId, "ROLE_USER"));
    }

    @Test
    void attachTags_success_addsTagsAndPublishesEvent() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String tagName = "important";

        Application app = new Application();
        app.setApplicantId(actorId); // actor — заявитель
        app.setTags(new HashSet<>());

        when(applicationRepository.findByIdWithTags(appId)).thenReturn(Optional.of(app));

        useCase.attachTags(appId, List.of(tagName), actorId, "ROLE_USER");

        // Проверяем, что тег добавлен
        assertTrue(app.getTags().contains(tagName));
        verify(applicationRepository).save(app);

        // Проверяем публикацию события
        ArgumentCaptor<TagEvent> captor = ArgumentCaptor.forClass(TagEvent.class);
        verify(eventPublisher).publishTagAttachRequest(captor.capture());
        TagEvent event = captor.getValue();
        assertEquals(appId, event.getApplicationId());
        assertEquals(actorId, event.getActorId());
        assertTrue(event.getTagNames().contains(tagName));
    }

    @Test
    void attachTags_eventPublishingFails_exceptionIgnored() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String tagName = "urgent";

        Application app = new Application();
        app.setApplicantId(actorId);
        app.setTags(new HashSet<>());

        when(applicationRepository.findByIdWithTags(appId)).thenReturn(Optional.of(app));
        doThrow(new RuntimeException("Publish failed")).when(eventPublisher).publishTagAttachRequest(any());

        // Метод должен завершиться без исключения
        assertDoesNotThrow(() ->
                useCase.attachTags(appId, List.of(tagName), actorId, "ROLE_USER"));

        // Тег всё равно добавлен
        assertTrue(app.getTags().contains(tagName));
        verify(applicationRepository).save(app);
        verify(eventPublisher).publishTagAttachRequest(any());
    }

    @Test
    void attachTags_adminOrManager_canAttachTags() {
        UUID appId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Application app = new Application();
        app.setApplicantId(applicantId);
        app.setTags(new HashSet<>());
        when(applicationRepository.findByIdWithTags(appId)).thenReturn(Optional.of(app));

        // Admin
        useCase.attachTags(appId, List.of("adminTag"), adminId, "ROLE_ADMIN");
        assertTrue(app.getTags().contains("adminTag"));

        // Manager
        useCase.attachTags(appId, List.of("managerTag"), adminId, "ROLE_MANAGER");
        assertTrue(app.getTags().contains("managerTag"));
    }
}
