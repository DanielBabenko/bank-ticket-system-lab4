package com.example.applicationservice.application.usecase;

import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetApplicationUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private GetApplicationUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        useCase = new GetApplicationUseCase(applicationRepository);
    }

    @Test
    void findById_foundInWithFiles_returnsApplication() {
        UUID appId = UUID.randomUUID();
        Application app = new Application();
        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(Optional.of(app));

        Optional<Application> result = useCase.findById(appId);

        assertTrue(result.isPresent());
        assertEquals(app, result.get());
        verify(applicationRepository, times(1)).findByIdWithFiles(appId);
        verify(applicationRepository, never()).findById(any());
    }

    @Test
    void findById_notFoundInWithFiles_foundInBasic_returnsApplication() {
        UUID appId = UUID.randomUUID();
        Application app = new Application();
        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(Optional.empty());
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        Optional<Application> result = useCase.findById(appId);

        assertTrue(result.isPresent());
        assertEquals(app, result.get());
        verify(applicationRepository).findByIdWithFiles(appId);
        verify(applicationRepository).findById(appId);
    }

    @Test
    void findById_notFoundAnywhere_returnsEmpty() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findByIdWithFiles(appId)).thenReturn(Optional.empty());
        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        Optional<Application> result = useCase.findById(appId);

        assertTrue(result.isEmpty());
        verify(applicationRepository).findByIdWithFiles(appId);
        verify(applicationRepository).findById(appId);
    }
}
