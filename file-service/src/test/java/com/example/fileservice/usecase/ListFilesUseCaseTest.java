package com.example.fileservice.usecase;

import com.example.fileservice.application.usecase.ListFilesUseCase;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListFilesUseCaseTest {

    @Mock
    private FileRepositoryPort repository;

    private ListFilesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListFilesUseCase(repository);
    }

    @Test
    void listAll_invalidSize_throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.listAll(0, 0));
    }

    @Test
    void listAll_returnsList() {
        File f = new File(); f.setId(UUID.randomUUID());
        when(repository.findAll(0, 10)).thenReturn(Collections.singletonList(f));

        List<File> res = useCase.listAll(0, 10);
        assertEquals(1, res.size());
        verify(repository).findAll(0, 10);
    }
}
