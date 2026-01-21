package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.dto.ApplicationCreateCommand;
import com.example.applicationservice.domain.event.FileEvent;
import com.example.applicationservice.domain.event.TagEvent;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.model.enums.ApplicationStatus;
import com.example.applicationservice.domain.model.enums.UserRole;
import com.example.applicationservice.domain.port.outbound.*;
import com.example.applicationservice.domain.port.inbound.CreateApplicationUseCasePort;

import java.time.Instant;
import java.util.*;

public class CreateApplicationUseCase implements CreateApplicationUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final ApplicationHistoryRepositoryPort historyRepository;
    private final UserServicePort userService;
    private final ProductServicePort productService;
    private final FileServicePort fileService;
    private final EventPublisherPort eventPublisher;

    public CreateApplicationUseCase(
            ApplicationRepositoryPort applicationRepository,
            ApplicationHistoryRepositoryPort historyRepository,
            UserServicePort userService,
            ProductServicePort productService,
            FileServicePort fileService,
            EventPublisherPort eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.userService = userService;
        this.productService = productService;
        this.fileService = fileService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Application createApplication(ApplicationCreateCommand command, UUID actorId, String actorRoleClaim) {
        if (command == null) throw new BadRequestException("Request is required");

        UUID applicantId = command.getApplicantId();
        UUID productId = command.getProductId();
        List<UUID> files = command.getFiles() != null ? command.getFiles() : List.of();
        List<String> tags = command.getTags() != null ? command.getTags() : List.of();

        if (applicantId == null || productId == null) {
            throw new BadRequestException("Applicant ID and Product ID are required");
        }

        boolean isAdmin = "ROLE_ADMIN".equals(actorRoleClaim);
        if (!isAdmin && !actorId.equals(applicantId)) {
            throw new ForbiddenException("You can create an application only for yourself");
        }

        // check user exists
        Boolean userExists;
        try {
            userExists = userService.userExists(applicantId);
        } catch (Exception e) {
            throw new ServiceUnavailableException("User service is unavailable now");
        }
        if (userExists == null) throw new ServiceUnavailableException("User service is unavailable now");
        if (!userExists) throw new NotFoundException("Applicant with this ID not found");

        // check product exists
        Boolean productExists;
        try {
            productExists = productService.productExists(productId);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Product service is unavailable now");
        }
        if (productExists == null) throw new ServiceUnavailableException("Product service is unavailable now");
        if (!productExists) throw new NotFoundException("Product with this ID not found");

        // create domain application
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setApplicantId(applicantId);
        app.setProductId(productId);
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setCreatedAt(Instant.now());
        app.setFiles(new HashSet<>(files));
        app.setTags(new HashSet<>(tags));

        // persist
        applicationRepository.save(app);

        // persist history
        ApplicationHistory hist = new ApplicationHistory();
        hist.setId(UUID.randomUUID());
        hist.setApplicationId(app.getId());
        hist.setOldStatus(null);
        hist.setNewStatus(app.getStatus());
        hist.setChangedBy(UserRole.ROLE_CLIENT);
        hist.setChangedAt(Instant.now());
        historyRepository.save(hist);

        // publish file attach event (non-blocking in adapters)
        try {
            FileEvent fe = new FileEvent(UUID.randomUUID(), "FILE_ATTACH_REQUEST", app.getId(), actorId, files);
            eventPublisher.publishFileAttachRequest(fe);
        } catch (Exception e) {
            // логируем в adapters; в application слое просто проигнорируем публикацию
        }

        // publish tag create request (non-blocking in adapters)
        if (!tags.isEmpty()) {
            try {
                TagEvent te = new TagEvent(UUID.randomUUID(), "TAG_CREATE_REQUEST", app.getId(), actorId, tags);
                eventPublisher.publishTagCreateRequest(te);
            } catch (Exception e) {
                // ignore publishing error here
            }
        }

        return app;
    }
}
