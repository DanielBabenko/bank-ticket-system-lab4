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

class FindApplicationsByTagUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private FindApplicationsByTagUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        useCase = new FindApplicationsByTagUseCase(applicationRepository);
    }

    @Test
    void findApplicationsByTag_success_returnsApplicationInfoList() {
        UUID appId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String tagName = "important";

        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(applicantId);
        app.setProductId(productId);
        app.setStatus(ApplicationStatus.APPROVED);
        app.setCreatedAt(Instant.now());

        when(applicationRepository.findByTag(tagName)).thenReturn(List.of(app));

        List<ApplicationInfo> result = useCase.findApplicationsByTag(tagName);

        assertEquals(1, result.size());
        ApplicationInfo info = result.get(0);
        assertEquals(appId, info.getId());
        assertEquals(applicantId, info.getApplicantId());
        assertEquals(productId, info.getProductId());
        assertEquals("APPROVED", info.getStatus());
        assertEquals(app.getCreatedAt(), info.getCreatedAt());
    }

    @Test
    void findApplicationsByTag_emptyList_returnsEmptyList() {
        String tagName = "nonexistent";
        when(applicationRepository.findByTag(tagName)).thenReturn(List.of());

        List<ApplicationInfo> result = useCase.findApplicationsByTag(tagName);

        assertTrue(result.isEmpty());
    }

    @Test
    void findApplicationsByTag_repositoryThrowsException_throwsBadRequestException() {
        String tagName = "failtag";
        when(applicationRepository.findByTag(tagName)).thenThrow(new RuntimeException("DB error"));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                useCase.findApplicationsByTag(tagName));

        assertTrue(ex.getMessage().contains("Failed to get applications by tag"));
        assertTrue(ex.getMessage().contains("DB error"));
    }

    @Test
    void findApplicationsByTag_statusNull_setsStatusNull() {
        String tagName = "nullstatus";
        UUID appId = UUID.randomUUID();
        Application app = new Application();
        app.setId(appId);
        app.setApplicantId(UUID.randomUUID());
        app.setProductId(UUID.randomUUID());
        app.setStatus(null);
        app.setCreatedAt(Instant.now());

        when(applicationRepository.findByTag(tagName)).thenReturn(List.of(app));

        List<ApplicationInfo> result = useCase.findApplicationsByTag(tagName);

        assertEquals(1, result.size());
        assertNull(result.get(0).getStatus());
    }
}
