package com.example.tagservice.usecase;

import com.example.tagservice.application.usecase.ListTagsUseCase;
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
public class ListTagsUseCaseTest {

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    private ListTagsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListTagsUseCase(tagRepositoryPort);
    }

    @Test
    void listAll_invalidSize_throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.listAll(0, 0));
    }

    @Test
    void listAll_returnsList() {
        Tag t1 = new Tag(); t1.setId(UUID.randomUUID()); t1.setName("t1");
        Tag t2 = new Tag(); t2.setId(UUID.randomUUID()); t2.setName("t2");

        when(tagRepositoryPort.findAll(0, 10)).thenReturn(Arrays.asList(t1, t2));

        List<Tag> result = useCase.listAll(0, 10);

        assertEquals(2, result.size());
        verify(tagRepositoryPort).findAll(0, 10);
    }
}
