package com.example.tagservice.usecase;

import com.example.tagservice.application.usecase.CreateOrGetTagsBatchUseCase;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateOrGetTagsBatchUseCaseTest {

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    private CreateOrGetTagsBatchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateOrGetTagsBatchUseCase(tagRepositoryPort);
    }

    @Test
    void createOrGetTags_nullInput_returnsEmpty() {
        List<Tag> result = useCase.createOrGetTags(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepositoryPort);
    }

    @Test
    void createOrGetTags_emptyInput_returnsEmpty() {
        List<Tag> result = useCase.createOrGetTags(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepositoryPort);
    }

    @Test
    void createOrGetTags_returnsExistingOnly() {
        List<String> names = Arrays.asList("a", "b");
        Tag a = new Tag(); a.setId(UUID.randomUUID()); a.setName("a");
        Tag b = new Tag(); b.setId(UUID.randomUUID()); b.setName("b");

        when(tagRepositoryPort.findByNames(names)).thenReturn(Arrays.asList(a, b));

        List<Tag> result = useCase.createOrGetTags(names);

        assertEquals(2, result.size());
        assertTrue(result.contains(a));
        assertTrue(result.contains(b));
        verify(tagRepositoryPort).findByNames(names);
        verify(tagRepositoryPort, never()).saveAll(anyList());
    }
}
