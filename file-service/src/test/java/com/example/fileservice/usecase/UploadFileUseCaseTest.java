package com.example.fileservice.usecase;

import com.example.fileservice.application.command.UploadFileCommand;
import com.example.fileservice.application.usecase.UploadFileUseCase;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadFileUseCaseTest {

    @Mock
    private FileRepositoryPort fileRepository;

    @Mock
    private StoragePort storagePort;

    private UploadFileUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UploadFileUseCase(fileRepository, storagePort);
    }

    @Test
    void uploadFile_nullCommand_throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.uploadFile(null));
    }

    @Test
    void uploadFile_nullStream_throws() {
        UUID uid = UUID.randomUUID();
        UploadFileCommand cmd = new UploadFileCommand(null, "a.txt", 10L, "text/plain", uid, "d");
        assertThrows(IllegalArgumentException.class, () -> useCase.uploadFile(cmd));
    }

    @Test
    void uploadFile_blankName_throws() {
        UUID uid = UUID.randomUUID();
        UploadFileCommand cmd = new UploadFileCommand(new ByteArrayInputStream(new byte[0]), "   ", 0L, "text/plain", uid, null);
        assertThrows(IllegalArgumentException.class, () -> useCase.uploadFile(cmd));
    }

    @Test
    void uploadFile_success_callsStorageAndRepository() throws Exception {
        UUID uid = UUID.randomUUID();
        InputStream is = new ByteArrayInputStream("hello".getBytes());
        UploadFileCommand cmd = new UploadFileCommand(is, "hello.txt", 5L, "text/plain", uid, "desc");

        // simulate repository save returning same object (assigns id)
        when(fileRepository.save(any(File.class))).thenAnswer(invocation -> {
            File f = invocation.getArgument(0);
            // repository may set fields, but we just return the same object
            return f;
        });

        // storagePort.upload does not throw
        doNothing().when(storagePort).upload(anyString(), anyString(), any(InputStream.class), anyLong(), anyString());

        File result = useCase.uploadFile(cmd);

        assertNotNull(result);
        assertEquals("hello.txt", result.getOriginalName());
        assertEquals(uid, result.getUploaderId());
        verify(storagePort).upload(anyString(), anyString(), any(InputStream.class), eq(5L), eq("text/plain"));
        verify(fileRepository).save(any(File.class));
    }
}
