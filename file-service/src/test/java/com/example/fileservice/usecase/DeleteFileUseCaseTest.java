package com.example.fileservice.usecase;

import com.example.fileservice.application.exception.ForbiddenException;
import com.example.fileservice.application.exception.NotFoundException;
import com.example.fileservice.application.usecase.DeleteFileUseCase;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteFileUseCaseTest {

    @Mock
    private FileRepositoryPort fileRepository;

    @Mock
    private StoragePort storagePort;

    private DeleteFileUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteFileUseCase(fileRepository, storagePort);
    }

    @Test
    void deleteFile_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.deleteFile(null, UUID.randomUUID(), false));
    }

    @Test
    void deleteFile_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(fileRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> useCase.deleteFile(id, UUID.randomUUID(), false));
    }

    @Test
    void deleteFile_notOwner_throwsForbidden() throws Exception {
        UUID id = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        File f = new File();
        f.setId(id);
        f.setUploaderId(owner);

        when(fileRepository.findById(id)).thenReturn(Optional.of(f));

        assertThrows(ForbiddenException.class, () -> useCase.deleteFile(id, other, false));
        verify(storagePort, never()).delete(anyString(), anyString());
        verify(fileRepository, never()).delete(any());
    }

    @Test
    void deleteFile_owner_success_callsStorageAndRepo() throws Exception {
        UUID id = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        File f = new File();
        f.setId(id);
        f.setUploaderId(owner);
        f.setStorageKey("sk");
        f.setBucketName("files");

        when(fileRepository.findById(id)).thenReturn(Optional.of(f));
        doNothing().when(storagePort).delete(f.getBucketName(), f.getStorageKey());

        useCase.deleteFile(id, owner, false);

        verify(storagePort).delete(f.getBucketName(), f.getStorageKey());
        verify(fileRepository).delete(f);
    }
}
