package com.example.fileservice.usecase;

import com.example.fileservice.application.exception.NotFoundException;
import com.example.fileservice.application.usecase.GetFileUseCase;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
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
public class GetFileUseCaseTest {

    @Mock
    private FileRepositoryPort repository;

    private GetFileUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetFileUseCase(repository);
    }

    @Test
    void getFileById_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.getFileById(null));
    }

    @Test
    void getFileById_notFound_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> useCase.getFileById(id));
    }

    @Test
    void getFileById_found_returnsFile() {
        UUID id = UUID.randomUUID();
        File f = new File(); f.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(f));
        File res = useCase.getFileById(id);
        assertEquals(f, res);
    }
}
