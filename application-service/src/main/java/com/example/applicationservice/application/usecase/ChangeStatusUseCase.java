package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.model.enums.ApplicationStatus;
import com.example.applicationservice.domain.model.enums.UserRole;
import com.example.applicationservice.domain.port.inbound.ChangeStatusUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Change status of application.
 */
public class ChangeStatusUseCase implements ChangeStatusUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final ApplicationHistoryRepositoryPort historyRepository;

    public ChangeStatusUseCase(ApplicationRepositoryPort applicationRepository, ApplicationHistoryRepositoryPort historyRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public Application changeStatus(UUID applicationId, String status, UUID actorId, String actorRoleClaim) {
        if (actorId == null) throw new UnauthorizedException("Authentication required");

        boolean isManager = "ROLE_MANAGER".equals(actorRoleClaim);
        boolean isAdmin = "ROLE_ADMIN".equals(actorRoleClaim);
        if (!isManager && !isAdmin) throw new ForbiddenException("Only admin or manager can change application status");

        Application basicApp = applicationRepository.findById(applicationId).orElseThrow(() -> new NotFoundException("Application not found"));

        if (basicApp.getApplicantId().equals(actorId) && isManager) {
            throw new ConflictException("Managers cannot change status of their own applications");
        }

        Optional<Application> appWithDocs = applicationRepository.findByIdWithFiles(applicationId);
        Application app = appWithDocs.orElse(basicApp);
        applicationRepository.findByIdWithTags(applicationId).ifPresent(a -> app.setTags(a.getTags()));

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            throw new ConflictException("Invalid status. Valid values: DRAFT, SUBMITTED, IN_REVIEW, APPROVED, REJECTED");
        }

        ApplicationStatus oldStatus = app.getStatus();
        if (oldStatus != newStatus) {
            app.setStatus(newStatus);
            app.setUpdatedAt(Instant.now());
            applicationRepository.save(app);

            ApplicationHistory hist = new ApplicationHistory();
            hist.setId(UUID.randomUUID());
            hist.setApplicationId(app.getId());
            hist.setOldStatus(oldStatus);
            hist.setNewStatus(newStatus);
            hist.setChangedBy(enumFromRoleString(actorRoleClaim));
            hist.setChangedAt(Instant.now());
            historyRepository.save(hist);
        }

        return app;
    }

    private UserRole enumFromRoleString(String roleStr) {
        if (roleStr == null) return UserRole.ROLE_ADMIN;
        try { return UserRole.valueOf(roleStr); } catch (Exception e) { return UserRole.ROLE_ADMIN; }
    }
}
