package com.example.tagservice.adapters.inbound.web;

import com.example.tagservice.application.dto.TagDto;
import com.example.tagservice.application.mapper.TagMapper;
import com.example.tagservice.domain.model.ApplicationInfo;
import com.example.tagservice.domain.model.Tag;
import com.example.tagservice.domain.port.inbound.CreateOrGetTagsBatchUseCasePort;
import com.example.tagservice.domain.port.inbound.CreateTagUseCasePort;
import com.example.tagservice.domain.port.inbound.GetTagUseCasePort;
import com.example.tagservice.domain.port.inbound.ListTagsUseCasePort;
import com.example.tagservice.domain.port.outbound.ApplicationServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "API for managing tags")
@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private static final Logger log = LoggerFactory.getLogger(TagController.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final CreateTagUseCasePort createTagUseCase;
    private final ListTagsUseCasePort listTagsUseCase;
    private final GetTagUseCasePort getTagUseCase;
    private final CreateOrGetTagsBatchUseCasePort createOrGetTagsBatchUseCase;
    private final ApplicationServicePort applicationServicePort;

    public TagController(CreateTagUseCasePort createTagUseCase,
                         ListTagsUseCasePort listTagsUseCase,
                         GetTagUseCasePort getTagUseCase,
                         CreateOrGetTagsBatchUseCasePort createOrGetTagsBatchUseCase,
                         ApplicationServicePort applicationServicePort) {
        this.createTagUseCase = createTagUseCase;
        this.listTagsUseCase = listTagsUseCase;
        this.getTagUseCase = getTagUseCase;
        this.createOrGetTagsBatchUseCase = createOrGetTagsBatchUseCase;
        this.applicationServicePort = applicationServicePort;
    }

    @Operation(summary = "Create a new  unique tag", description = "Registers a new tag: name if it has not already existed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tag created or found successfully")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody String name,
                                            UriComponentsBuilder uriBuilder) {
        String trimmed = (name == null) ? "" : name.trim();
        com.example.tagservice.domain.model.Tag tag = createTagUseCase.createIfNotExists(trimmed);

        // не делаем внешний вызов application-service для простого create
        TagDto dto = TagMapper.toDtoWithoutApplications(tag);

        URI location = uriBuilder.path("/api/v1/tags/{name}")
                .buildAndExpand(dto.getName())
                .toUri();

        log.info("Tag created or retrieved: {}", trimmed);
        return ResponseEntity.created(location).body(dto);
    }

    @Operation(summary = "Read all tags", description = "Returns list of tags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of applications"),
            @ApiResponse(responseCode = "400", description = "Page size too large")
    })
    @GetMapping
    public ResponseEntity<List<TagDto>> listTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(String.format("Page size cannot be greater than %d", MAX_PAGE_SIZE));
        }

        List<Tag> tags = listTagsUseCase.listAll(page, size);

        List<TagDto> dtos = tags.stream()
                .map(tag -> {
                    List<ApplicationInfo> apps = applicationServicePort.getApplicationsByTag(tag.getName());
                    return TagMapper.toDto(tag, apps);
                })
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(dtos.size()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(dtos);
    }

    @Operation(summary = "Read certain tag by its name", description = "Returns data about a single tag: name and list of applications that uses this tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data about a single tag"),
            @ApiResponse(responseCode = "404", description = "Tag with this name is not found")
    })
    @GetMapping("/{name}")
    public ResponseEntity<TagDto> getTagWithApplications(@PathVariable String name) {
        Tag tag = getTagUseCase.getTagByName(name);

        // Для детального запроса достаём список applications из внешнего сервиса
        List<ApplicationInfo> apps = applicationServicePort.getApplicationsByTag(tag.getName());
        TagDto dto = TagMapper.toDto(tag, apps);

        log.info("Returning tag {} with {} applications", name, dto.getApplications() == null ? 0 : dto.getApplications().size());
        return ResponseEntity.ok(dto);
    }

    // internal-запрос для application-service
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<TagDto>> createOrGetTagsBatch(
            @Valid @RequestBody List<String> tagNames) {

        if (tagNames == null || tagNames.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Tag> tags = createOrGetTagsBatchUseCase.createOrGetTags(tagNames);

        List<TagDto> dtos = tags.stream()
                .map(TagMapper::toDtoWithoutApplications)
                .collect(Collectors.toList());

        log.info("Processed batch of {} tags", dtos.size());
        return ResponseEntity.ok(dtos);
    }
}
