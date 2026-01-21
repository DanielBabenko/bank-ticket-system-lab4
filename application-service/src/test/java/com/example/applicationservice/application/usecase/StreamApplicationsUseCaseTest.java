package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.BadRequestException;
import com.example.applicationservice.application.usecase.StreamApplicationsUseCase;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.enums.ApplicationStatus;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.FileServicePort;
import com.example.applicationservice.application.util.CursorUtil;
import com.example.applicationservice.domain.util.ApplicationPage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StreamApplicationsUseCaseTest {

    @Mock ApplicationRepositoryPort repo;
    @Mock FileServicePort fileService;
    StreamApplicationsUseCase usecase;

    @BeforeEach
    void setUp() {
        usecase = new StreamApplicationsUseCase(repo, fileService);
    }

    @Test
    void invalidLimit_throws() {
        assertThrows(BadRequestException.class, () -> usecase.streamWithNextCursor(null, 0));
    }

    @Test
    void stream_firstPage_success() {
        UUID app1 = UUID.randomUUID();
        UUID f1 = UUID.randomUUID();
        Application a1 = new Application();
        a1.setId(app1);
        a1.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        a1.setFiles(new HashSet<>(List.of(f1)));

        when(repo.findIdsFirstPage(5)).thenReturn(List.of(app1));
        when(repo.findByIdsWithFiles(List.of(app1))).thenReturn(List.of(a1));
        when(repo.findByIdsWithTags(List.of(app1))).thenReturn(List.of(a1));
        when(fileService.checkFilesExist(List.of(f1))).thenReturn(List.of(f1));

        var page = usecase.streamWithNextCursor(null, 5);
        assertNotNull(page);
        assertFalse(page.getItems().isEmpty());
        assertNotNull(page.getNextCursor());
    }

    @Test
    void stream_keyset_callsKeysetRepo() {
        Instant ts = Instant.parse("2024-01-01T00:00:05Z");
        UUID cursorId = UUID.randomUUID();
        String cursor = CursorUtil.encode(ts, cursorId);
        UUID appId = UUID.randomUUID();
        UUID f1 = UUID.randomUUID();

        when(repo.findIdsByKeyset(ts, cursorId, 5)).thenReturn(List.of(appId));
        Application a = new Application();
        a.setId(appId);
        a.setCreatedAt(Instant.parse("2024-01-01T00:00:04Z"));
        a.setFiles(new HashSet<>(List.of(f1)));
        when(repo.findByIdsWithFiles(List.of(appId))).thenReturn(List.of(a));
        when(repo.findByIdsWithTags(List.of(appId))).thenReturn(List.of(a));
        when(fileService.checkFilesExist(List.of(f1))).thenReturn(List.of(f1));

        var page = usecase.streamWithNextCursor(cursor, 5);
        assertNotNull(page);
        assertEquals(1, page.getItems().size());
    }
}
