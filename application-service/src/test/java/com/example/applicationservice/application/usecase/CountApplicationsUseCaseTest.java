package com.example.applicationservice.application.usecase;

import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CountApplicationsUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private CountApplicationsUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        useCase = new CountApplicationsUseCase(applicationRepository);
    }

    @Test
    void count_returnsRepositoryCount() {
        // arrange
        long expectedCount = 42L;
        when(applicationRepository.count()).thenReturn(expectedCount);

        // act
        long result = useCase.count();

        // assert
        assertEquals(expectedCount, result);
        verify(applicationRepository, times(1)).count();
    }

    @Test
    void count_zeroApplications_returnsZero() {
        when(applicationRepository.count()).thenReturn(0L);

        long result = useCase.count();

        assertEquals(0L, result);
        verify(applicationRepository).count();
    }
}
