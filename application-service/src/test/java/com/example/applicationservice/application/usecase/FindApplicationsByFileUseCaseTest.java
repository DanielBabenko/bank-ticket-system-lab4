package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.BadRequestException;
import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.enums.ApplicationStatus;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindApplicationsByFileUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private FindApplicationsByFileUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        useCase = new FindApplicationsByFileUseCase(applicationRepository);
    }

    @Test
    void findApplicationsByFile_success_returnsApplicationInfoList() {
        UUID fileId = UUID.randomUUID();
        UUID appId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(applicantId);
        app.setProductId(productId);
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setCreatedAt(Instant.now());

        when(applicationRepository.findByFile(fileId)).thenReturn(List.of(app));

        List<ApplicationInfo> result = useCase.findApplicationsByFile(fileId);

        assertEquals(1, result.size());
        ApplicationInfo info = result.get(0);
        assertEquals(appId, info.getId());
        assertEquals(applicantId, info.getApplicantId());
        assertEquals(productId, info.getProductId());
        assertEquals("SUBMITTED", info.getStatus());
        assertEquals(app.getCreatedAt(), info.getCreatedAt());
    }

    @Test
    void findApplicationsByFile_emptyList_returnsEmptyList() {
        UUID fileId = UUID.randomUUID();
        when(applicationRepository.findByFile(fileId)).thenReturn(List.of());

        List<ApplicationInfo> result = useCase.findApplicationsByFile(fileId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findApplicationsByFile_repositoryThrowsException_throwsBadRequestException() {
        UUID fileId = UUID.randomUUID();
        when(applicationRepository.findByFile(fileId)).thenThrow(new RuntimeException("DB error"));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                useCase.findApplicationsByFile(fileId));

        assertTrue(ex.getMessage().contains("Failed to get applications by file"));
        assertTrue(ex.getMessage().contains("DB error"));
    }
}
