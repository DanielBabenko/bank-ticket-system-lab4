package com.example.tagservice.usecase;

import com.example.tagservice.application.usecase.CreateTagUseCase;
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
public class CreateTagUseCaseTest {

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    private CreateTagUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateTagUseCase(tagRepositoryPort);
    }

    @Test
    void createIfNotExists_whenNameNull_throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.createIfNotExists(null));
    }

    @Test
    void createIfNotExists_whenBlank_throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.createIfNotExists("   "));
    }

    @Test
    void createIfNotExists_creates_whenNotExists() {
        String name = "urgent";
        when(tagRepositoryPort.findByName(name)).thenReturn(Optional.empty());
        when(tagRepositoryPort.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        Tag result = useCase.createIfNotExists(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertNotNull(result.getId());
        verify(tagRepositoryPort).findByName(name);
        verify(tagRepositoryPort).save(any(Tag.class));
    }

    @Test
    void createIfNotExists_returnsExisting_whenExists() {
        String name = "existing";
        Tag existing = new Tag();
        existing.setId(UUID.randomUUID());
        existing.setName(name);

        when(tagRepositoryPort.findByName(name)).thenReturn(Optional.of(existing));

        Tag result = useCase.createIfNotExists(name);

        assertSame(existing, result);
        verify(tagRepositoryPort).findByName(name);
        verify(tagRepositoryPort, never()).save(any());
    }

    @Test
    void createIfNotExists_trimsName() {
        String input = "  spaced  ";
        String expected = "spaced";
        when(tagRepositoryPort.findByName(expected)).thenReturn(Optional.empty());
        when(tagRepositoryPort.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        Tag result = useCase.createIfNotExists(input);

        assertEquals(expected, result.getName());
    }
}
