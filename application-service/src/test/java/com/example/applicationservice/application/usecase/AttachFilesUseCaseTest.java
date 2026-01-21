package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.ForbiddenException;
import com.example.applicationservice.application.exception.NotFoundException;
import com.example.applicationservice.domain.event.FileEvent;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.EventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttachFilesUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private EventPublisherPort eventPublisher;
    private AttachFilesUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        eventPublisher = mock(EventPublisherPort.class);
        useCase = new AttachFilesUseCase(applicationRepository, eventPublisher);
    }

    @Test
    void attachFiles_applicationNotFound_throwsNotFoundException() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                useCase.attachFiles(appId, List.of(UUID.randomUUID()), UUID.randomUUID(), "ROLE_USER"));
    }

    @Test
    void attachFiles_insufficientPermissions_throwsForbiddenException() {
        UUID appId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        Application app = new Application();
        app.setApplicantId(applicantId);
        app.setFiles(new HashSet<>());
        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(Optional.of(app));

        UUID actorId = UUID.randomUUID(); // не совпадает с applicantId
        assertThrows(ForbiddenException.class, () ->
                useCase.attachFiles(appId, List.of(UUID.randomUUID()), actorId, "ROLE_USER"));
    }

    @Test
    void attachFiles_success_addsFilesAndPublishesEvent() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        Application app = new Application();
        app.setApplicantId(actorId); // actor — заявитель
        app.setFiles(new HashSet<>());

        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(Optional.of(app));

        useCase.attachFiles(appId, List.of(fileId), actorId, "ROLE_USER");

        // Проверяем, что файл добавлен
        assertTrue(app.getFiles().contains(fileId));
        verify(applicationRepository).save(app);

        // Проверяем публикацию события
        ArgumentCaptor<FileEvent> captor = ArgumentCaptor.forClass(FileEvent.class);
        verify(eventPublisher).publishFileAttachRequest(captor.capture());
        FileEvent event = captor.getValue();
        assertEquals(appId, event.getApplicationId());
        assertEquals(actorId, event.getActorId());
        assertTrue(event.getFileIds().contains(fileId));
    }

    @Test
    void attachFiles_eventPublishingFails_exceptionIgnored() {
        UUID appId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        Application app = new Application();
        app.setApplicantId(actorId);
        app.setFiles(new HashSet<>());

        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(Optional.of(app));
        doThrow(new RuntimeException("Publish failed")).when(eventPublisher).publishFileAttachRequest(any());

        // Метод должен завершиться без исключения
        assertDoesNotThrow(() ->
                useCase.attachFiles(appId, List.of(fileId), actorId, "ROLE_USER"));

        // Файл всё равно добавлен
        assertTrue(app.getFiles().contains(fileId));
        verify(applicationRepository).save(app);
        verify(eventPublisher).publishFileAttachRequest(any());
    }
}
