package com.example.tagservice.usecase;

import com.example.tagservice.application.service.CreateOrGetTagsBatchUseCase;
import com.example.tagservice.application.service.CreateTagUseCase;
import com.example.tagservice.application.service.GetTagUseCase;
import com.example.tagservice.application.service.ListTagsUseCase;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.outbound.ApplicationServicePort;
import com.example.tagservice.domain.port.outbound.TagRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationUseCasesTest {

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @Mock
    private ApplicationServicePort applicationServicePort; // понадобился для некоторых тестов

    private CreateTagUseCase createTagUseCase;
    private CreateOrGetTagsBatchUseCase createOrGetTagsBatchUseCase;
    private ListTagsUseCase listTagsUseCase;
    private GetTagUseCase getTagUseCase;

    private UUID testId;
    private String tagName;

    @BeforeEach
    void setUp() {
        createTagUseCase = new CreateTagUseCase(tagRepositoryPort);
        createOrGetTagsBatchUseCase = new CreateOrGetTagsBatchUseCase(tagRepositoryPort);
        listTagsUseCase = new ListTagsUseCase(tagRepositoryPort);
        getTagUseCase = new GetTagUseCase(tagRepositoryPort);

        testId = UUID.randomUUID();
        tagName = "test-tag";
    }

    // -----------------------
    // CreateTagUseCase tests
    // -----------------------
    @Test
    void createIfNotExists_createsNewTagWhenNotExists() {
        String name = "urgent";

        when(tagRepositoryPort.findByName(name)).thenReturn(Optional.empty());
        when(tagRepositoryPort.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag t = invocation.getArgument(0);
            t.setId(testId);
            return t;
        });

        Tag result = createTagUseCase.createIfNotExists(name);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(name, result.getName());

        verify(tagRepositoryPort, times(1)).findByName(name);
        verify(tagRepositoryPort, times(1)).save(any(Tag.class));
    }

    @Test
    void createIfNotExists_returnsExistingTagWhenExists() {
        Tag existing = new Tag();
        existing.setId(testId);
        existing.setName(tagName);

        when(tagRepositoryPort.findByName(tagName)).thenReturn(Optional.of(existing));

        Tag result = createTagUseCase.createIfNotExists(tagName);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(tagName, result.getName());

        verify(tagRepositoryPort, times(1)).findByName(tagName);
        verify(tagRepositoryPort, never()).save(any());
    }

    @Test
    void createIfNotExists_trimsName() {
        String input = "  urgent  ";
        String expected = "urgent";

        when(tagRepositoryPort.findByName(expected)).thenReturn(Optional.empty());
        when(tagRepositoryPort.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag t = invocation.getArgument(0);
            t.setId(testId);
            return t;
        });

        Tag result = createTagUseCase.createIfNotExists(input);
        assertEquals(expected, result.getName());
    }

    // -----------------------
    // CreateOrGetTagsBatchUseCase tests
    // -----------------------
    @Test
    void createOrGetTags_returnsEmptyForNull() {
        var res = createOrGetTagsBatchUseCase.createOrGetTags(null);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(tagRepositoryPort);
    }

    @Test
    void createOrGetTags_returnsExistingOnly() {
        List<String> names = Arrays.asList("a","b");
        Tag t1 = new Tag(); t1.setId(UUID.randomUUID()); t1.setName("a");
        Tag t2 = new Tag(); t2.setId(UUID.randomUUID()); t2.setName("b");

        when(tagRepositoryPort.findByNames(names)).thenReturn(Arrays.asList(t1,t2));

        List<Tag> res = createOrGetTagsBatchUseCase.createOrGetTags(names);
        assertEquals(2, res.size());
        assertTrue(res.contains(t1));
        assertTrue(res.contains(t2));
        verify(tagRepositoryPort, times(1)).findByNames(names);
        verify(tagRepositoryPort, never()).saveAll(anyList());
    }
/*
    @Test
    void createOrGetTags_createsMissing() {
        List<String> names = Arrays.asList("t1","t2");
        Tag existing = new Tag(); existing.setId(UUID.randomUUID()); existing.setName("t1");
        when(tagRepositoryPort.findByNames(names)).thenReturn(Collections.singletonList(existing));
        when(tagRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> {
            List<Tag> list = invocation.getArgument(0);
            list.forEach(t -> t.setId(UUID.randomUUID()));
            return list;
        });

        List<Tag> res = createOrGetTagsBatchUseCase.createOrGetTags(names);
        assertEquals(2, res.size());
        assertTrue(res.stream().anyMatch(t -> "t1".equals(t.getName())));
        assertTrue(res.stream().anyMatch(t -> "t2".equals(t.getName())));
        verify(tagRepositoryPort, times(1)).findByNames(names);
        verify(tagRepositoryPort, times(1)).saveAll(anyList());
    }

    @Test
    void createOrGetTags_trimsAndDedupes() {
        List<String> input = Arrays.asList("  tag1  ", "tag1", " tag2 ", "tag2");
        List<String> expected = Arrays.asList("tag1","tag2");

        when(tagRepositoryPort.findByNames(expected)).thenReturn(Collections.emptyList());
        when(tagRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> {
            List<Tag> lst = invocation.getArgument(0);
            lst.forEach(t -> t.setId(UUID.randomUUID()));
            return lst;
        });

        List<Tag> res = createOrGetTagsBatchUseCase.createOrGetTags(input);
        assertEquals(2, res.size());
        verify(tagRepositoryPort, times(1)).findByNames(expected);
    }
*/
    // -----------------------
    // ListTagsUseCase tests
    // -----------------------
    @Test
    void listAll_returnsTagsPage() {
        List<Tag> sample = List.of(newTag("a"), newTag("b"));
        when(tagRepositoryPort.findAll(0, 10)).thenReturn(sample);

        List<Tag> res = listTagsUseCase.listAll(0, 10);

        assertEquals(2, res.size());
    }

    // -----------------------
    // GetTagUseCase tests
    // -----------------------
    @Test
    void getTagByName_returnsTagWhenExists() {
        Tag t = newTag(tagName);
        when(tagRepositoryPort.findByName(tagName)).thenReturn(Optional.of(t));

        Tag res = getTagUseCase.getTagByName(tagName);
        assertEquals(tagName, res.getName());
    }

    @Test
    void getTagByName_throwsWhenNotFound() {
        when(tagRepositoryPort.findByName("nope")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> getTagUseCase.getTagByName("nope"));
        assertNotNull(ex.getMessage());
    }

    // helper
    private Tag newTag(String name) {
        Tag t = new Tag();
        t.setId(UUID.randomUUID());
        t.setName(name);
        return t;
    }
}
