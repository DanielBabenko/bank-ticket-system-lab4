package com.example.applicationservice.adapters.inbound.rest;

import com.example.applicationservice.application.dto.*;
import com.example.applicationservice.application.mapper.ApplicationMapper;
import com.example.applicationservice.domain.dto.ApplicationCreateCommand;
import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.port.inbound.*;
import com.example.applicationservice.application.exception.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Applications", description = "API for managing applications")
@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final CreateApplicationUseCasePort createPort;
    private final ListApplicationsUseCasePort listPort;
    private final GetApplicationUseCasePort getPort;
    private final StreamApplicationsUseCasePort streamPort;
    private final AttachTagsUseCasePort attachTagsPort;
    private final RemoveTagsUseCasePort removeTagsPort;
    private final AttachFilesUseCasePort attachFilesPort;
    private final RemoveFilesUseCasePort removeFilesPort;
    private final ChangeStatusUseCasePort changeStatusPort;
    private final DeleteApplicationUseCasePort deletePort;
    private final DeleteApplicationsByUserIdUseCasePort deleteByUserPort;
    private final DeleteApplicationsByProductIdUseCasePort deleteByProductPort;
    private final FindApplicationsByTagUseCasePort findByTagPort;
    private final FindApplicationsByFileUseCasePort findByFilePort;
    private final ListHistoryUseCasePort listHistoryPort;
    private final CountApplicationsUseCasePort countPort;

    public ApplicationController(
            CreateApplicationUseCasePort createPort,
            ListApplicationsUseCasePort listPort,
            GetApplicationUseCasePort getPort,
            StreamApplicationsUseCasePort streamPort,
            AttachTagsUseCasePort attachTagsPort,
            RemoveTagsUseCasePort removeTagsPort,
            AttachFilesUseCasePort attachFilesPort,
            RemoveFilesUseCasePort removeFilesPort,
            ChangeStatusUseCasePort changeStatusPort,
            DeleteApplicationUseCasePort deletePort,
            DeleteApplicationsByUserIdUseCasePort deleteByUserPort,
            DeleteApplicationsByProductIdUseCasePort deleteByProductPort,
            FindApplicationsByTagUseCasePort findByTagPort,
            FindApplicationsByFileUseCasePort findByFilePort,
            ListHistoryUseCasePort listHistoryPort,
            CountApplicationsUseCasePort countPort
    ) {
        this.createPort = createPort;
        this.listPort = listPort;
        this.getPort = getPort;
        this.streamPort = streamPort;
        this.attachTagsPort = attachTagsPort;
        this.removeTagsPort = removeTagsPort;
        this.attachFilesPort = attachFilesPort;
        this.removeFilesPort = removeFilesPort;
        this.changeStatusPort = changeStatusPort;
        this.deletePort = deletePort;
        this.deleteByUserPort = deleteByUserPort;
        this.deleteByProductPort = deleteByProductPort;
        this.findByTagPort = findByTagPort;
        this.findByFilePort = findByFilePort;
        this.listHistoryPort = listHistoryPort;
        this.countPort = countPort;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApplicationDto> createApplication(
            @Valid @RequestBody com.example.applicationservice.application.dto.ApplicationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return Mono.error(new UnauthorizedException("Authentication required"));
        }
        String uid = jwt.getClaimAsString("uid");
        if (uid == null) uid = jwt.getSubject();
        UUID actorId = UUID.fromString(uid);
        String roleStr = jwt.getClaimAsString("role");

        ApplicationCreateCommand cmd = new ApplicationCreateCommand();
        cmd.setApplicantId(request.getApplicantId());
        cmd.setProductId(request.getProductId());
        cmd.setFiles(request.getFiles());
        cmd.setTags(request.getTags());

        return Mono.fromCallable(() -> createPort.createApplication(cmd, actorId, roleStr))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ApplicationMapper::toDto);
    }

    @GetMapping
    public Flux<ApplicationDto> listApplications(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        if (size > MAX_PAGE_SIZE) {
            return Flux.error(new BadRequestException(String.format("Page size cannot be greater than %d", MAX_PAGE_SIZE)));
        }

        return Mono.fromCallable(() -> listPort.listApplications(page, size))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(ApplicationMapper::toDto);
    }

    @GetMapping("/{id}")
    public Mono<ApplicationDto> getApplication(@PathVariable UUID id) {
        return Mono.fromCallable(() -> getPort.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(opt -> opt.map(app -> Mono.just(ApplicationMapper.toDto(app))).orElseGet(() -> Mono.error(new com.example.applicationservice.application.exception.NotFoundException("Application with this ID not found"))));
    }

    @GetMapping("/stream")
    public Mono<com.example.applicationservice.domain.util.ApplicationPage> streamApplications(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {

        if (limit > MAX_PAGE_SIZE) {
            return Mono.error(new BadRequestException(String.format("Limit cannot be greater than %d", MAX_PAGE_SIZE)));
        }

        // We keep cursor handling in adapter (controller) for compatibility with previous implementation.
        return Mono.fromCallable(() -> streamPort.streamWithNextCursor(cursor, limit))
                .subscribeOn(Schedulers.boundedElastic())
                .map(domainPage -> {
                    // map domain.ApplicationInfo -> application.dto.ApplicationInfoDto
                    List<ApplicationInfo> items = domainPage.getItems();
                    return new com.example.applicationservice.domain.util.ApplicationPage(items, domainPage.getNextCursor());
                });
    }

    @PutMapping("/{id}/tags")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> addTags(@PathVariable UUID id, @RequestBody List<String> tags, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return Mono.error(new UnauthorizedException("Authentication required"));
        String uid = jwt.getClaimAsString("uid");
        if (uid == null) uid = jwt.getSubject();
        UUID actorId = UUID.fromString(uid);
        String roleStr = jwt.getClaimAsString("role");

        return Mono.fromRunnable(() -> attachTagsPort.attachTags(id, tags, actorId, roleStr))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @DeleteMapping("/{id}/tags")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeTags(@PathVariable UUID id, @RequestBody List<String> tags, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return Mono.error(new UnauthorizedException("Authentication required"));
        String uid = jwt.getClaimAsString("uid");
        if (uid == null) uid = jwt.getSubject();
        UUID actorId = UUID.fromString(uid);
        String roleStr = jwt.getClaimAsString("role");

        return Mono.fromRunnable(() -> removeTagsPort.removeTags(id, tags, actorId, roleStr))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @PutMapping("/{id}/files")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> addFiles(@PathVariable UUID id, @RequestBody List<UUID> files, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return Mono.error(new UnauthorizedException("Authentication required"));
        String uid = jwt.getClaimAsString("uid");
        if (uid == null) uid = jwt.getSubject();
        UUID actorId = UUID.fromString(uid);
        String roleStr = jwt.getClaimAsString("role");

        return Mono.fromRunnable(() -> attachFilesPort.attachFiles(id, files, actorId, roleStr))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @DeleteMapping("/{id}/files")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeFiles(@PathVariable UUID id, @RequestBody List<UUID> files, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return Mono.error(new UnauthorizedException("Authentication required"));
        String uid = jwt.getClaimAsString("uid");
        if (uid == null) uid = jwt.getSubject();
        UUID actorId = UUID.fromString(uid);
        String roleStr = jwt.getClaimAsString("role");

        return Mono.fromRunnable(() -> removeFilesPort.removeFiles(id, files, actorId, roleStr))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @PutMapping("/{id}/status")
    public Mono<ApplicationDto> changeStatus(@PathVariable UUID id, @RequestBody String status, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return Mono.error(new UnauthorizedException("Authentication required"));
        String uid = jwt.getClaimAsString("uid");
        if (uid == null) uid = jwt.getSubject();
        UUID actorId = UUID.fromString(uid);
        String roleStr = jwt.getClaimAsString("role");

        return Mono.fromCallable(() -> changeStatusPort.changeStatus(id, status, actorId, roleStr))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ApplicationMapper::toDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteApplication(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return Mono.error(new UnauthorizedException("Authentication required"));
        String uid = jwt.getClaimAsString("uid");
        if (uid == null) uid = jwt.getSubject();
        UUID actorId = UUID.fromString(uid);
        String roleStr = jwt.getClaimAsString("role");

        return Mono.fromRunnable(() -> deletePort.deleteApplication(id, actorId, roleStr))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    // Internal endpoints
    @DeleteMapping("/internal/by-user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteApplicationsByUserId(@RequestParam("userId") UUID userId) {
        return Mono.fromRunnable(() -> deleteByUserPort.deleteApplicationsByUserId(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @DeleteMapping("/internal/by-product")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteApplicationsByProductId(@RequestParam("productId") UUID productId) {
        return Mono.fromRunnable(() -> deleteByProductPort.deleteApplicationsByProductId(productId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @GetMapping("/by-tag")
    public Mono<List<ApplicationInfoDto>> getApplicationsByTag(@RequestParam("tag") String tagName) {
        return Mono.fromCallable(() -> findByTagPort.findApplicationsByTag(tagName))
                .subscribeOn(Schedulers.boundedElastic())
                .map(list -> list.stream().map(ai -> {
                    ApplicationInfoDto dto = new ApplicationInfoDto();
                    dto.setId(ai.getId());
                    dto.setApplicantId(ai.getApplicantId());
                    dto.setProductId(ai.getProductId());
                    dto.setStatus(ai.getStatus());
                    dto.setCreatedAt(ai.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList()));
    }

    @GetMapping("/by-file")
    public Mono<List<ApplicationInfoDto>> getApplicationsByFile(@RequestParam("file") UUID fileId) {
        return Mono.fromCallable(() -> findByFilePort.findApplicationsByFile(fileId))
                .subscribeOn(Schedulers.boundedElastic())
                .map(list -> list.stream().map(ai -> {
                    ApplicationInfoDto dto = new ApplicationInfoDto();
                    dto.setId(ai.getId());
                    dto.setApplicantId(ai.getApplicantId());
                    dto.setProductId(ai.getProductId());
                    dto.setStatus(ai.getStatus());
                    dto.setCreatedAt(ai.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList()));
    }

    @GetMapping("/{id}/history")
    public Flux<ApplicationHistoryDto> getApplicationHistory(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return Flux.error(new UnauthorizedException("Authentication required"));
        String uid = jwt.getClaimAsString("uid");
        if (uid == null) uid = jwt.getSubject();
        UUID actorId = UUID.fromString(uid);
        String roleStr = jwt.getClaimAsString("role");

        return Mono.fromCallable(() -> listHistoryPort.listHistory(id, actorId, roleStr))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(ApplicationMapper::toHistoryDto);
    }
}
