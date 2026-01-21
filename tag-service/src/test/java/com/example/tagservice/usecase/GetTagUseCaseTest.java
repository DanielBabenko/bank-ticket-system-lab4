package com.example.tagservice.usecase;

import com.example.tagservice.application.exception.NotFoundException;
import com.example.tagservice.application.usecase.GetTagUseCase;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
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
public class GetTagUseCaseTest {

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    private GetTagUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetTagUseCase(tagRepositoryPort);
    }

    @Test
    void getTagByName_nullOrBlank_throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.getTagByName(null));
        assertThrows(IllegalArgumentException.class, () -> useCase.getTagByName("   "));
    }

    @Test
    void getTagByName_notFound_throwsNotFound() {
        String name = "no";
        when(tagRepositoryPort.findByName(name)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> useCase.getTagByName(name));
        assertTrue(ex.getMessage().contains("Tag not found"));
        verify(tagRepositoryPort).findByName(name);
    }

    @Test
    void getTagByName_found_returnsTag() {
        String name = "ok";
        Tag t = new Tag(); t.setId(UUID.randomUUID()); t.setName(name);
        when(tagRepositoryPort.findByName(name)).thenReturn(Optional.of(t));

        Tag result = useCase.getTagByName(name);
        assertEquals(t, result);
        verify(tagRepositoryPort).findByName(name);
    }
}
