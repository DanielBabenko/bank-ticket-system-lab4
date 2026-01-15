package com.example.applicationservice.service;

import com.example.applicationservice.dto.*;
import com.example.applicationservice.event.FileEvent;
import com.example.applicationservice.event.TagEvent;
import com.example.applicationservice.exception.*;
import com.example.applicationservice.feign.*;
import com.example.applicationservice.model.entity.*;
import com.example.applicationservice.model.enums.ApplicationStatus;
import com.example.applicationservice.model.enums.UserRole;
import com.example.applicationservice.repository.*;
import com.example.applicationservice.util.ApplicationPage;
import com.example.applicationservice.util.CursorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            ApplicationHistoryRepository applicationHistoryRepository,
            UserServiceClient userServiceClient,
            ProductServiceClient productServiceClient, KafkaSender<String, String> kafkaSender, ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.applicationHistoryRepository = applicationHistoryRepository;
        this.userServiceClient = userServiceClient;
        this.productServiceClient = productServiceClient;
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
    }

    @Value("${spring.kafka.topics.tag-create-request:tag.create.request}")
    private String tagCreateRequestTopic;

    @Value("${spring.kafka.topics.tag-attach-request:tag.attach.request}")
    private String tagAttachRequestTopic;

    @Value("${spring.kafka.topics.file-attach-request:file.attach.request}")
    private String fileAttachRequestTopic;

    /**
     * Create application.
     * actorId - id of the authenticated user (from JWT)
     * actorRoleClaim - role string from JWT (may be null)
     */
    public Mono<ApplicationDto> createApplication(ApplicationRequest req, UUID actorId, String actorRoleClaim) {
        if (req == null) {
            return Mono.error(new BadRequestException("Request is required"));
        }

        UUID applicantId = req.getApplicantId();
        UUID productId = req.getProductId();

        if (applicantId == null || productId == null) {
            return Mono.error(new BadRequestException("Applicant ID and Product ID are required"));
        }

        // Authorization
        boolean isAdmin = "ROLE_ADMIN".equals(actorRoleClaim);
        if (!isAdmin && !actorId.equals(applicantId)) {
            return Mono.error(new ForbiddenException("You can create an application only for yourself"));
        }

        return Mono.fromCallable(() -> {
                    try {
                        return userServiceClient.userExists(applicantId);
                    } catch (FeignException.NotFound | NotFoundException ex) {
                        throw new NotFoundException("Applicant with this ID not found");
                    } catch (Exception ex) {
                        throw new ServiceUnavailableException("User service is unavailable now");
                    }
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(userExists -> {
                    if (userExists == null) {
                        return Mono.error(new ServiceUnavailableException("User service is unavailable now"));
                    }
                    if (!userExists) {
                        return Mono.error(new NotFoundException("Applicant with this ID not found"));
                    }
                    return Mono.fromCallable(() -> {
                        try {
                            return productServiceClient.productExists(productId);
                        } catch (FeignException.NotFound | NotFoundException ex) {
                            throw new NotFoundException("Product with this ID not found");
                        } catch (Exception ex) {
                            throw new ServiceUnavailableException("Product service is unavailable now");
                        }
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .flatMap(productExists -> {
                    if (productExists == null) {
                        return Mono.error(new ServiceUnavailableException("Product service is unavailable now"));
                    }
                    if (!productExists) {
                        return Mono.error(new NotFoundException("Product with this ID not found"));
                    }

                    return Mono.fromCallable(() -> {
                        Application app = new Application();
                        app.setId(UUID.randomUUID());
                        app.setApplicantId(applicantId);
                        app.setProductId(productId);
                        app.setStatus(ApplicationStatus.SUBMITTED);
                        app.setCreatedAt(Instant.now());

                        List<UUID> fileIds = req.getFiles() != null ? req.getFiles() : List.of();
                        app.setFiles(new HashSet<>(fileIds));
                        sendFileAttachRequest(app.getId(), actorId, fileIds);

                        applicationRepository.save(app);

                        ApplicationHistory hist = new ApplicationHistory();
                        hist.setId(UUID.randomUUID());
                        hist.setApplication(app);
                        hist.setOldStatus(null);
                        hist.setNewStatus(app.getStatus());
                        hist.setChangedBy(UserRole.ROLE_CLIENT);
                        hist.setChangedAt(Instant.now());
                        applicationHistoryRepository.save(hist);

                        log.info("Application created: {}", app.getId());

                        return app;
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .flatMap(app -> {
                    List<String> tagNames = req.getTags() != null ? req.getTags() : List.of();
                    if (!tagNames.isEmpty()) {
                        // Асинхронно отправляем запрос на создание тегов
                        return sendTagCreateRequest(app.getId(), actorId, tagNames)
                                .then(Mono.fromCallable(() -> {
                                    // Сохраняем теги как строки (не ждем ответа от tag-service)
                                    app.setTags(new HashSet<>(tagNames));
                                    applicationRepository.save(app);
                                    log.info("Tags {} queued for creation for application {}", tagNames, app.getId());
                                    return app;
                                }).subscribeOn(Schedulers.boundedElastic()));
                    }
                    return Mono.just(app);
                })
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Flux<ApplicationDto> findAll(int page, int size) {
        if (size > 50) {
            return Flux.error(new BadRequestException("Page size cannot exceed 50"));
        }
        return Mono.fromCallable(() -> {
                    Pageable pageable = PageRequest.of(page, size);
                    Page<Application> applicationsPage = applicationRepository.findAll(pageable);
                    List<Application> applications = applicationsPage.getContent();
                    if (applications.isEmpty()) {
                        return List.<ApplicationDto>of();
                    }
                    List<UUID> applicationIds = applications.stream()
                            .map(Application::getId)
                            .collect(Collectors.toList());
                    List<Application> appsWithTags = applicationRepository.findByIdsWithTags(applicationIds);
                    Map<UUID, Set<String>> tagsMap = new HashMap<>();
                    for (Application appWithTags : appsWithTags) {
                        tagsMap.put(appWithTags.getId(), appWithTags.getTags());
                    }
                    List<Application> appsWithFiles = applicationRepository.findByIdsWithFiles(applicationIds);
                    Map<UUID, Set<UUID>> filesMap = new HashMap<>();
                    for (Application appWithFiles : appsWithFiles) {
                        filesMap.put(appWithFiles.getId(), appWithFiles.getFiles());
                    }
                    return applications.stream()
                            .map(app -> {
                                Set<String> tags = tagsMap.get(app.getId());
                                Set<UUID> files = filesMap.get(app.getId());
                                if (tags != null) {
                                    app.setTags(tags);
                                }
                                if (files != null) {
                                    app.setFiles(files);
                                }
                                return toDto(app);
                            })
                            .collect(Collectors.toList());
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    @Transactional(readOnly = true)
    public Mono<ApplicationDto> findById(UUID id) {
        return Mono.fromCallable(() -> {
            Optional<Application> appWithDocs = applicationRepository.findByIdWithFiles(id);
            if (appWithDocs.isEmpty()) {
                throw new NotFoundException("Application with this ID not found");
            }
            Application app = appWithDocs.get();
            Optional<Application> appWithTags = applicationRepository.findByIdWithTags(id);
            appWithTags.ifPresent(appWithTag -> app.setTags(appWithTag.getTags()));
            return toDto(app);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<ApplicationPage> streamWithNextCursor(String cursor, int limit) {
        if (limit <= 0) {
            return Mono.error(new BadRequestException("limit must be greater than 0"));
        }
        int capped = Math.min(limit, 50);
        final Instant[] tsHolder = new Instant[1];
        final UUID[] idHolder = new UUID[1];
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtil.Decoded decoded = CursorUtil.decode(cursor);
                if (decoded != null) {
                    tsHolder[0] = decoded.timestamp;
                    idHolder[0] = decoded.id;
                }
            } catch (Exception e) {
                return Mono.error(new BadRequestException("Invalid cursor format: " + e.getMessage()));
            }
        }
        return Mono.fromCallable(() -> {
            Instant ts = tsHolder[0];
            UUID id = idHolder[0];
            List<UUID> appIds;
            if (ts == null) {
                appIds = applicationRepository.findIdsFirstPage(capped);
            } else {
                appIds = applicationRepository.findIdsByKeyset(ts, id, capped);
            }
            if (appIds.isEmpty()) {
                return new ApplicationPage(List.of(), null);
            }
            List<Application> appsWithDocs = applicationRepository.findByIdsWithFiles(appIds);
            List<Application> appsWithTags = applicationRepository.findByIdsWithTags(appIds);
            Map<UUID, Application> appMap = new HashMap<>();
            for (Application app : appsWithDocs) {
                appMap.put(app.getId(), app);
            }
            for (Application appWithTags : appsWithTags) {
                Application app = appMap.get(appWithTags.getId());
                if (app != null) {
                    app.setTags(appWithTags.getTags());
                }
            }
            List<Application> apps = appIds.stream()
                    .map(appMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            List<ApplicationDto> dtos = apps.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            String nextCursor = null;
            if (!apps.isEmpty()) {
                Application last = apps.get(apps.size() - 1);
                nextCursor = CursorUtil.encode(last.getCreatedAt(), last.getId());
            }
            return new ApplicationPage(dtos, nextCursor);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // attachTags now receives actorId and actorRoleClaim
    @Transactional
    public Mono<Void> attachTags(UUID applicationId, List<String> tagNames, UUID actorId, String actorRoleClaim) {
        return validateActor(applicationId, actorId, actorRoleClaim)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new ForbiddenException("Insufficient permissions"));
                    }
                    return Mono.fromCallable(() -> {
                        Application app = applicationRepository.findByIdWithTags(applicationId)
                                .orElseThrow(() -> new NotFoundException("Application not found"));

                        // Обновляем теги локально
                        if (app.getTags() == null) app.setTags(new HashSet<>());
                        app.getTags().addAll(tagNames);
                        applicationRepository.save(app);

                        log.info("Added {} tags to existing application {} (async)", tagNames.size(), applicationId);

                        // Асинхронно отправляем запрос на создание тегов
                        sendTagAttachRequest(applicationId, actorId, tagNames);

                        return (Void) null;
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    // attachFiles now receives actorId and actorRoleClaim
    @Transactional
    public Mono<Void> attachFiles(UUID applicationId, List<UUID> fileIds, UUID actorId, String actorRoleClaim) {
        return validateActor(applicationId, actorId, actorRoleClaim)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new ForbiddenException("Insufficient permissions"));
                    }
                    return Mono.fromCallable(() -> {
                        Application app = applicationRepository.findByIdWithFiles(applicationId)
                                .orElseThrow(() -> new NotFoundException("Application not found"));

                        // Обновляем теги локально
                        if (app.getFiles() == null) app.setFiles(new HashSet<>());
                        app.getFiles().addAll(fileIds);
                        applicationRepository.save(app);

                        log.info("Added {} files to existing application {} (async)", fileIds.size(), applicationId);

                        // Асинхронно отправляем запрос на создание тегов
                        sendFileAttachRequest(applicationId, actorId, fileIds);

                        return (Void) null;
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    /**
     * Отправка запроса на создание тегов
     */
    private Mono<Void> sendTagCreateRequest(UUID applicationId, UUID actorId, List<String> tagNames) {
        return Mono.fromRunnable(() -> {
            try {
                TagEvent event = new TagEvent(
                        UUID.randomUUID(),
                        "TAG_CREATE_REQUEST",
                        applicationId,
                        actorId,
                        tagNames
                );

                String message = objectMapper.writeValueAsString(event);

                SenderRecord<String, String, String> record = SenderRecord.create(
                        tagCreateRequestTopic,
                        null,
                        System.currentTimeMillis(),
                        event.getEventId().toString(),
                        message,
                        null
                );

                kafkaSender.send(Mono.just(record))
                        .doOnNext(result -> {
                            if (result.exception() == null) {
                                log.info("Tag create request sent for application: {}, tags: {}",
                                        applicationId, tagNames);
                            } else {
                                log.error("Failed to send tag create request: {}",
                                        result.exception().getMessage());
                            }
                        })
                        .subscribe();

            } catch (Exception e) {
                log.error("Error sending tag create request: {}", e.getMessage());
            }
        });
    }

    /**
     * Отправка запроса на прикрепление тегов
     */
    private void sendTagAttachRequest(UUID applicationId, UUID actorId, List<String> tagNames) {
        try {
            TagEvent event = new TagEvent(
                    UUID.randomUUID(),
                    "TAG_ATTACH_REQUEST",
                    applicationId,
                    actorId,
                    tagNames
            );

            String message = objectMapper.writeValueAsString(event);

            SenderRecord<String, String, String> record = SenderRecord.create(
                    tagAttachRequestTopic,
                    null,
                    System.currentTimeMillis(),
                    event.getEventId().toString(),
                    message,
                    null
            );

            kafkaSender.send(Mono.just(record))
                    .doOnNext(result -> {
                        if (result.exception() == null) {
                            log.info("Tag attach request sent for application: {}, tags: {}",
                                    applicationId, tagNames);
                        } else {
                            log.error("Failed to send tag attach request: {}",
                                    result.exception().getMessage());
                        }
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("Error sending tag attach request: {}", e.getMessage());
        }
    }

    /**
     * Отправка запроса на прикрепление тегов
     */
    private void sendFileAttachRequest(UUID applicationId, UUID actorId, List<UUID> fileIds) {
        try {
            FileEvent event = new FileEvent(
                    UUID.randomUUID(),
                    "FILE_ATTACH_REQUEST",
                    applicationId,
                    actorId,
                    fileIds
            );

            String message = objectMapper.writeValueAsString(event);

            SenderRecord<String, String, String> record = SenderRecord.create(
                    fileAttachRequestTopic,
                    null,
                    System.currentTimeMillis(),
                    event.getEventId().toString(),
                    message,
                    null
            );

            kafkaSender.send(Mono.just(record))
                    .doOnNext(result -> {
                        if (result.exception() == null) {
                            log.info("File attach request sent for application: {}, files: {}",
                                    applicationId, fileIds);
                        } else {
                            log.error("Failed to send file attach request: {}",
                                    result.exception().getMessage());
                        }
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("Error sending file attach request: {}", e.getMessage());
        }
    }

    @Transactional
    public Mono<Void> removeTags(UUID applicationId, List<String> tagNames, UUID actorId, String actorRoleClaim) {
        return validateActor(applicationId, actorId, actorRoleClaim)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new ForbiddenException("Insufficient permissions"));
                    }
                    return Mono.fromCallable(() -> {
                        Application app = applicationRepository.findByIdWithTags(applicationId)
                                .orElseThrow(() -> new NotFoundException("Application not found"));
                        tagNames.forEach(n -> {
                            if (app.getTags() != null) app.getTags().remove(n);
                        });
                        applicationRepository.save(app);
                        log.info("Removed {} tags from application {}", tagNames.size(), applicationId);
                        return (Void) null;
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    @Transactional
    public Mono<Void> removeFiles(UUID applicationId, List<UUID> fileIds, UUID actorId, String actorRoleClaim) {
        return validateActor(applicationId, actorId, actorRoleClaim)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new ForbiddenException("Insufficient permissions"));
                    }
                    return Mono.fromCallable(() -> {
                        Application app = applicationRepository.findByIdWithFiles(applicationId)
                                .orElseThrow(() -> new NotFoundException("Application not found"));
                        fileIds.forEach(n -> {
                            if (app.getFiles() != null) app.getFiles().remove(n);
                        });
                        applicationRepository.save(app);
                        log.info("Removed {} files from application {}", fileIds.size(), applicationId);
                        return (Void) null;
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    @Transactional
    public Mono<ApplicationDto> changeStatus(UUID applicationId, String status, UUID actorId, String actorRoleClaim) {
        if (actorId == null) {
            return Mono.error(new UnauthorizedException("Authentication required"));
        }
        // derive role
        boolean isManager = "ROLE_MANAGER".equals(actorRoleClaim);
        boolean isAdmin = "ROLE_ADMIN".equals(actorRoleClaim);
        if (!isManager && !isAdmin) {
            return Mono.error(new ForbiddenException("Only admin or manager can change application status"));
        }

        return Mono.fromCallable(() -> {
            Application basicApp = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new NotFoundException("Application not found"));
            if (basicApp.getApplicantId().equals(actorId) && isManager) {
                throw new ConflictException("Managers cannot change status of their own applications");
            }
            Optional<Application> appWithDocs = applicationRepository.findByIdWithFiles(applicationId);
            Application app = appWithDocs.orElse(basicApp);
            Optional<Application> appWithTags = application_repository_findByIdWithTags_safe(applicationId);
            appWithTags.ifPresent(appWithTag -> app.setTags(appWithTag.getTags()));
            ApplicationStatus newStatus;
            try {
                newStatus = ApplicationStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ConflictException(
                        "Invalid status. Valid values: DRAFT, SUBMITTED, IN_REVIEW, APPROVED, REJECTED");
            }
            ApplicationStatus oldStatus = app.getStatus();
            if (oldStatus != newStatus) {
                app.setStatus(newStatus);
                app.setUpdatedAt(Instant.now());
                applicationRepository.save(app);
                ApplicationHistory hist = new ApplicationHistory();
                hist.setId(UUID.randomUUID());
                hist.setApplication(app);
                hist.setOldStatus(oldStatus);
                hist.setNewStatus(newStatus);
                // record who changed - use enum from actorRoleClaim if possible, else use ADMIN as fallback
                hist.setChangedBy(enumFromRoleString(actorRoleClaim));
                hist.setChangedAt(Instant.now());
                applicationHistoryRepository.save(hist);
                log.info("Application {} status changed from {} to {} by {}",
                        applicationId, oldStatus, newStatus, actorId);
            }
            return toDto(app);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> deleteApplication(UUID applicationId, UUID actorId, String actorRoleClaim) {
        boolean isAdmin = "ROLE_ADMIN".equals(actorRoleClaim);
        if (!isAdmin) {
            return Mono.error(new ForbiddenException("Only admin can delete applications"));
        }
        return Mono.fromCallable(() -> {
            applicationRepository.deleteFilesByApplicationId(applicationId);
            applicationHistoryRepository.deleteByApplicationId(applicationId);
            applicationRepository.deleteTagsByApplicationId(applicationId);
            applicationRepository.deleteById(applicationId);

            log.info("Application deleted: {}", applicationId);
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<ApplicationHistoryDto> listHistory(UUID applicationId, UUID actorId, String actorRoleClaim) {
        return validateActorCanViewHistory(applicationId, actorId, actorRoleClaim)
                .flatMapMany(canView -> {
                    if (!canView) {
                        return Flux.error(new ForbiddenException("Insufficient permissions to view history"));
                    }

                    return Mono.fromCallable(() ->
                                    applicationHistoryRepository.findByApplicationIdOrderByChangedAtDesc(applicationId)
                                            .stream()
                                            .map(this::toHistoryDto)
                                            .collect(Collectors.toList())
                            ).subscribeOn(Schedulers.boundedElastic())
                            .flatMapMany(Flux::fromIterable);
                });
    }

    @Transactional
    public Mono<Void> deleteApplicationsByUserId(UUID userId) {
        return Mono.fromCallable(() -> {
            List<UUID> applicationIds = applicationRepository.findIdsByApplicantId(userId);
            for (UUID appId : applicationIds) {
                applicationRepository.deleteFilesByApplicationId(appId);
                applicationHistoryRepository.deleteByApplicationId(appId);
                applicationRepository.deleteTagsByApplicationId(appId);
                applicationRepository.deleteById(appId);

                log.info("Deleted application {} for user {}", appId, userId);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> deleteApplicationsByProductId(UUID productId) {
        return Mono.fromCallable(() -> {
            List<UUID> productIds = applicationRepository.findIdsByProductId(productId);
            for (UUID appId : productIds) {
                applicationRepository.deleteFilesByApplicationId(appId);
                applicationHistoryRepository.deleteByApplicationId(appId);
                applicationRepository.deleteTagsByApplicationId(appId);
                applicationRepository.deleteById(appId);

                log.info("Deleted application {} for product {}", appId, productId);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<List<ApplicationInfoDto>> findApplicationsByTag(String tagName) {
        return Mono.fromCallable(() -> {
            try {
                List<Application> applications = applicationRepository.findByTag(tagName);
                List<ApplicationInfoDto> dtos = applications.stream()
                        .map(this::toInfoDto)
                        .collect(Collectors.toList());

                log.info("Found {} applications with tag {}", dtos.size(), tagName);
                return dtos;
            } catch (Exception e) {
                log.error("Failed to get applications by tag {}: {}", tagName, e.getMessage());
                throw new BadRequestException("Failed to get applications by tag: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<List<ApplicationInfoDto>> findApplicationsByFile(UUID fileId) {
        return Mono.fromCallable(() -> {
            try {
                List<Application> applications = applicationRepository.findByFile(fileId);
                List<ApplicationInfoDto> dtos = applications.stream()
                        .map(this::toInfoDto)
                        .collect(Collectors.toList());

                log.info("Found {} applications with file {}", dtos.size(), fileId);
                return dtos;
            } catch (Exception e) {
                log.error("Failed to get applications by file {}: {}", fileId, e.getMessage());
                throw new BadRequestException("Failed to get applications by tag: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // helper mapping methods unchanged
    private ApplicationInfoDto toInfoDto(Application app) {
        ApplicationInfoDto dto = new ApplicationInfoDto();
        dto.setId(app.getId());
        dto.setApplicantId(app.getApplicantId());
        dto.setProductId(app.getProductId());
        dto.setStatus(app.getStatus().toString());
        dto.setCreatedAt(app.getCreatedAt());
        return dto;
    }

    private ApplicationDto toDto(Application app) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(app.getId());
        dto.setApplicantId(app.getApplicantId());
        dto.setProductId(app.getProductId());
        dto.setStatus(app.getStatus());
        dto.setCreatedAt(app.getCreatedAt());

        if (app.getFiles() != null) {
            dto.setFiles(new ArrayList<>(app.getFiles()));
        }

        if (app.getTags() != null) {
            dto.setTags(new ArrayList<>(app.getTags()));
        }

        return dto;
    }

    private ApplicationHistoryDto toHistoryDto(ApplicationHistory history) {
        ApplicationHistoryDto dto = new ApplicationHistoryDto();
        dto.setId(history.getId());
        dto.setApplicationId(history.getApplication().getId());
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setChangedByRole(history.getChangedBy());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }

    // validateActor: use actorRoleClaim passed from JWT instead of calling userServiceClient.getUserRole(...)
    private Mono<Boolean> validateActor(UUID applicationId, UUID actorId, String actorRoleClaim) {
        return findById(applicationId)
                .flatMap(app -> Mono.fromCallable(() -> {
                    // if actor is the applicant -> allowed
                    if (app.getApplicantId().equals(actorId)) {
                        return true;
                    }
                    // if actor has admin or manager role (from token) -> allowed
                    return "ROLE_ADMIN".equals(actorRoleClaim) || "ROLE_MANAGER".equals(actorRoleClaim);
                }).subscribeOn(Schedulers.boundedElastic()))
                .defaultIfEmpty(false);
    }

    private Mono<Boolean> validateActorIsAdmin(UUID actorId, String actorRoleClaim) {
        return Mono.fromCallable(() -> "ROLE_ADMIN".equals(actorRoleClaim))
                .subscribeOn(Schedulers.boundedElastic())
                .defaultIfEmpty(false);
    }

    private Mono<Boolean> validateActorCanViewHistory(UUID applicationId, UUID actorId, String actorRoleClaim) {
        return findById(applicationId)
                .flatMap(app -> Mono.fromCallable(() -> {
                    if (app.getApplicantId().equals(actorId)) {
                        return true;
                    }
                    return "ROLE_ADMIN".equals(actorRoleClaim) || "ROLE_MANAGER".equals(actorRoleClaim);
                }).subscribeOn(Schedulers.boundedElastic()))
                .defaultIfEmpty(false);
    }

    public Mono<Long> count() {
        return Mono.fromCallable(applicationRepository::count
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // helper: safe fetch tags wrapper for earlier code compatibility
    private Optional<Application> application_repository_findByIdWithTags_safe(UUID applicationId) {
        try {
            return applicationRepository.findByIdWithTags(applicationId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private UserRole enumFromRoleString(String roleStr) {
        if (roleStr == null) return UserRole.ROLE_ADMIN; // fallback
        try {
            return UserRole.valueOf(roleStr);
        } catch (Exception e) {
            return UserRole.ROLE_ADMIN;
        }
    }
}