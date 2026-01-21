package com.example.fileservice.usecase;

import com.example.fileservice.application.usecase.GetFilesBatchUseCase;
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
public class GetFilesBatchUseCaseTest {

    @Mock
    private FileRepositoryPort repository;

    private GetFilesBatchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetFilesBatchUseCase(repository);
    }

    @Test
    void getFilesBatch_nullOrEmpty_returnsEmpty() {
        assertTrue(useCase.getFilesBatch(null).isEmpty());
        assertTrue(useCase.getFilesBatch(Collections.emptyList()).isEmpty());
    }

    @Test
    void getFilesBatch_returnsIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        File f1 = new File(); f1.setId(id1);
        File f2 = new File(); f2.setId(id2);

        when(repository.findByIds(Arrays.asList(id1, id2))).thenReturn(Arrays.asList(f1, f2));

        List<UUID> res = useCase.getFilesBatch(Arrays.asList(id1, id2));
        assertEquals(2, res.size());
        assertTrue(res.contains(id1));
        assertTrue(res.contains(id2));
    }
}
