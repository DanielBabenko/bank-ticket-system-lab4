package com.example.tagservice.controller;

import com.example.tagservice.adapters.inbound.web.TagController;
import com.example.tagservice.application.dto.TagDto;
import com.example.tagservice.domain.model.ApplicationInfo;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.CreateOrGetTagsBatchUseCasePort;
import com.example.tagservice.domain.port.inbound.CreateTagUseCasePort;
import com.example.tagservice.domain.port.inbound.GetTagUseCasePort;
import com.example.tagservice.domain.port.inbound.ListTagsUseCasePort;
import com.example.tagservice.domain.port.outbound.ApplicationServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagControllerTest {

    @Mock
    private CreateTagUseCasePort createTagUseCase;

    @Mock
    private ListTagsUseCasePort listTagsUseCase;

    @Mock
    private GetTagUseCasePort getTagUseCase;

    @Mock
    private CreateOrGetTagsBatchUseCasePort createOrGetTagsBatchUseCase;

    @Mock
    private ApplicationServicePort applicationServicePort;

    @InjectMocks
    private TagController tagController;

    private Tag sampleTagDomain;
    private TagDto sampleTagDto;

    @BeforeEach
    void setUp() {
        sampleTagDomain = new Tag();
        sampleTagDomain.setId(UUID.randomUUID());
        sampleTagDomain.setName("Test Tag");

        ApplicationInfo ai = new ApplicationInfo();
        ai.setId(UUID.randomUUID());
        ai.setApplicantId(UUID.randomUUID());
        ai.setProductId(UUID.randomUUID());
        ai.setStatus("SUBMITTED");
        ai.setCreatedAt(Instant.now());

        sampleTagDto = new TagDto();
        sampleTagDto.setId(sampleTagDomain.getId());
        sampleTagDto.setName(sampleTagDomain.getName());
        // applications will be tested via applicationServicePort -> domain.ApplicationInfo -> mapped in controller
    }

    @Test
    void createTag_validName_returnsCreated() {
        String name = "New Tag";
        sampleTagDomain.setName(name);

        when(createTagUseCase.createIfNotExists(name)).thenReturn(sampleTagDomain);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        ResponseEntity<TagDto> resp = tagController.createTag(name, uriBuilder);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(name, resp.getBody().getName());
        assertNotNull(resp.getHeaders().getLocation());
    }

    @Test
    void listTags_returnsDtosAndHeader() {
        Tag t1 = new Tag(); t1.setId(UUID.randomUUID()); t1.setName("t1");
        Tag t2 = new Tag(); t2.setId(UUID.randomUUID()); t2.setName("t2");

        when(listTagsUseCase.listAll(0, 20)).thenReturn(List.of(t1, t2));

        var resp = tagController.listTags(0, 20);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        assertEquals("2", resp.getHeaders().getFirst("X-Total-Count"));
    }

    @Test
    void getTagWithApplications_returnsDtoWithApplications() {
        String name = "some-tag";
        Tag t = new Tag(); t.setId(UUID.randomUUID()); t.setName(name);

        ApplicationInfo app1 = new ApplicationInfo();
        app1.setId(UUID.randomUUID());
        app1.setStatus("SUBMITTED");
        app1.setCreatedAt(Instant.now());

        when(getTagUseCase.getTagByName(name)).thenReturn(t);
        when(applicationServicePort.getApplicationsByTag(name)).thenReturn(List.of(app1));

        var resp = tagController.getTagWithApplications(name);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        TagDto dto = resp.getBody();
        assertNotNull(dto);
        assertEquals(name, dto.getName());
        assertNotNull(dto.getApplications());
        assertEquals(1, dto.getApplications().size());
    }

    @Test
    void createOrGetTagsBatch_returnsCreatedDtos() {
        List<String> names = List.of("a","b");
        Tag a = new Tag(); a.setId(UUID.randomUUID()); a.setName("a");
        Tag b = new Tag(); b.setId(UUID.randomUUID()); b.setName("b");

        when(createOrGetTagsBatchUseCase.createOrGetTags(names)).thenReturn(List.of(a,b));

        var resp = tagController.createOrGetTagsBatch(names);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        verify(createOrGetTagsBatchUseCase).createOrGetTags(names);
    }
}
