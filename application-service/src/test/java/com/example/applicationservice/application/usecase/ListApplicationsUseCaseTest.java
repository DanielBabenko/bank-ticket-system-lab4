package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.BadRequestException;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.FileServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListApplicationsUseCaseTest {

    private ApplicationRepositoryPort applicationRepository;
    private FileServicePort fileService;
    private ListApplicationsUseCase useCase;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepositoryPort.class);
        fileService = mock(FileServicePort.class);
        useCase = new ListApplicationsUseCase(applicationRepository, fileService);
    }

    @Test
    void listApplications_sizeExceeds50_throwsBadRequest() {
        assertThrows(BadRequestException.class, () ->
                useCase.listApplications(0, 51));
    }

    @Test
    void listApplications_emptyRepository_returnsEmptyList() {
        when(applicationRepository.findAll(0, 10)).thenReturn(Collections.emptyList());

        List<Application> result = useCase.listApplications(0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void listApplications_success_assignsTagsAndFiles() {
        UUID appId = UUID.randomUUID();
        UUID file1 = UUID.randomUUID();
        UUID file2 = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setFiles(Set.of(file1, file2));
        app.setTags(Set.of("tag1"));

        when(applicationRepository.findAll(0, 10)).thenReturn(List.of(app));
        when(applicationRepository.findByIdsWithTags(List.of(appId))).thenReturn(List.of(app));
        when(applicationRepository.findByIdsWithFiles(List.of(appId))).thenReturn(List.of(app));
        when(fileService.checkFilesExist(List.of(file1, file2))).thenReturn(List.of(file1)); // file2 не существует

        List<Application> result = useCase.listApplications(0, 10);

        assertEquals(1, result.size());
        Application resApp = result.get(0);

        // Проверка тегов
        assertEquals(Set.of("tag1"), resApp.getTags());

        // Проверка файлов: file2 отфильтрован
        assertEquals(Set.of(file1), resApp.getFiles());
    }

    @Test
    void listApplications_fileServiceFails_allFilesExist() {
        UUID appId = UUID.randomUUID();
        UUID file1 = UUID.randomUUID();

        Application app = new Application();
        app.setId(appId);
        app.setFiles(Set.of(file1));

        when(applicationRepository.findAll(0, 10)).thenReturn(List.of(app));
        when(applicationRepository.findByIdsWithTags(List.of(appId))).thenReturn(List.of(app));
        when(applicationRepository.findByIdsWithFiles(List.of(appId))).thenReturn(List.of(app));
        when(fileService.checkFilesExist(List.of(file1))).thenThrow(new RuntimeException("Service down"));

        List<Application> result = useCase.listApplications(0, 10);

        Application resApp = result.get(0);
        assertEquals(Set.of(file1), resApp.getFiles(), "All files should exist if service fails");
    }

    @Test
    void listApplications_multipleApplications_allCorrectlyMapped() {
        UUID app1Id = UUID.randomUUID();
        UUID app2Id = UUID.randomUUID();
        UUID f1 = UUID.randomUUID();
        UUID f2 = UUID.randomUUID();

        Application app1 = new Application();
        app1.setId(app1Id);
        app1.setFiles(Set.of(f1));
        app1.setTags(Set.of("t1"));

        Application app2 = new Application();
        app2.setId(app2Id);
        app2.setFiles(Set.of(f2));
        app2.setTags(Set.of("t2"));

        when(applicationRepository.findAll(0, 10)).thenReturn(List.of(app1, app2));
        when(applicationRepository.findByIdsWithTags(List.of(app1Id, app2Id))).thenReturn(List.of(app1, app2));
        when(applicationRepository.findByIdsWithFiles(List.of(app1Id, app2Id))).thenReturn(List.of(app1, app2));
        when(fileService.checkFilesExist(List.of(f1, f2))).thenReturn(List.of(f1, f2));

        List<Application> result = useCase.listApplications(0, 10);

        assertEquals(2, result.size());
        assertEquals(Set.of(f1), result.get(0).getFiles());
        assertEquals(Set.of(f2), result.get(1).getFiles());
        assertEquals(Set.of("t1"), result.get(0).getTags());
        assertEquals(Set.of("t2"), result.get(1).getTags());
    }
}
