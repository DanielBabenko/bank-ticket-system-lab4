package com.example.fileservice.usecase;

import com.example.fileservice.application.exception.ForbiddenException;
import com.example.fileservice.application.exception.NotFoundException;
import com.example.fileservice.application.usecase.DownloadFileUseCase;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DownloadFileUseCaseTest {

    @Mock
    private FileRepositoryPort fileRepository;

    @Mock
    private StoragePort storagePort;

    private DownloadFileUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DownloadFileUseCase(fileRepository, storagePort);
    }

    @Test
    void downloadFile_fileNotFound_throws() {
        UUID id = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        when(fileRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.downloadFile(id, user, false));
    }

    @Test
    void downloadFile_notOwner_throwsForbidden() throws Exception {
        UUID id = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        File f = new File();
        f.setId(id);
        f.setUploaderId(owner);

        when(fileRepository.findById(id)).thenReturn(Optional.of(f));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> useCase.downloadFile(id, other, false));
        assertTrue(ex.getMessage().contains("uploader"));
        verify(storagePort, never()).download(anyString(), anyString());
    }

    @Test
    void downloadFile_owner_success_returnsStream() throws Exception {
        UUID id = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        File f = new File();
        f.setId(id);
        f.setUploaderId(owner);
        f.setStorageKey("sk");
        f.setOriginalName("a.txt");
        f.setMimeType("text/plain");
        f.setSize(123L);

        when(fileRepository.findById(id)).thenReturn(Optional.of(f));
        when(storagePort.download(f.getBucketName(), f.getStorageKey())).thenReturn(new ByteArrayInputStream("ok".getBytes()));

        DownloadFileUseCase.DownloadResult res = useCase.downloadFile(id, owner, false);

        assertNotNull(res);
        assertEquals("a.txt", res.filename);
        assertEquals("text/plain", res.contentType);
        assertEquals(123L, res.size);
        assertNotNull(res.inputStream);
        verify(storagePort).download(f.getBucketName(), f.getStorageKey());
    }
}
