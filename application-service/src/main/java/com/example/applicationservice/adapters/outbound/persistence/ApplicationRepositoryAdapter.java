package com.example.applicationservice.adapters.outbound.persistence;

import com.example.applicationservice.adapters.outbound.persistence.entity.ApplicationEntity;
import com.example.applicationservice.adapters.outbound.persistence.entity.ApplicationHistoryEntity;
import com.example.applicationservice.adapters.outbound.persistence.jpa.ApplicationHistoryJpaRepository;
import com.example.applicationservice.adapters.outbound.persistence.jpa.ApplicationJpaRepository;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapter mapping JPA entity <-> domain model.
 */
@Component
public class ApplicationRepositoryAdapter implements ApplicationRepositoryPort {

    private final ApplicationJpaRepository jpa;
    private final ApplicationHistoryJpaRepository historyJpa;

    public ApplicationRepositoryAdapter(ApplicationJpaRepository jpa, ApplicationHistoryJpaRepository historyJpa) {
        this.jpa = jpa;
        this.historyJpa = historyJpa;
    }

    @Override
    public Application save(Application application) {
        ApplicationEntity entity = toEntity(application);
        ApplicationEntity saved = jpa.save(entity);
        // also persist history entities if present
        if (application.getHistory() != null && !application.getHistory().isEmpty()) {
            for (ApplicationHistory h : application.getHistory()) {
                ApplicationHistoryEntity he = toHistoryEntity(h, saved);
                historyJpa.save(he);
            }
        }
        return toDomain(saved);
    }

    @Override
    public Optional<Application> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Application> findByIdWithFiles(UUID id) {
        return jpa.findByIdWithFiles(id).map(this::toDomain);
    }

    @Override
    public Optional<Application> findByIdWithTags(UUID id) {
        return jpa.findByIdWithTags(id).map(this::toDomain);
    }

    @Override
    public List<Application> findAll(int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var pageRes = jpa.findAll(pageable);
        return pageRes.getContent().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<UUID> findIdsFirstPage(int limit) {
        return jpa.findIdsFirstPage(limit);
    }

    @Override
    public List<UUID> findIdsByKeyset(Instant timestamp, UUID id, int limit) {
        return jpa.findIdsByKeyset(timestamp, id, limit);
    }

    @Override
    public List<Application> findByIdsWithTags(List<UUID> ids) {
        return jpa.findByIdsWithTags(ids).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Application> findByIdsWithFiles(List<UUID> ids) {
        return jpa.findByIdsWithFiles(ids).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Application> findByTag(String tagName) {
        return jpa.findByTag(tagName).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Application> findByFile(UUID fileId) {
        return jpa.findByFile(fileId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteFilesByApplicationId(UUID applicationId) {
        jpa.deleteFilesByApplicationId(applicationId);
    }

    @Override
    public void deleteTagsByApplicationId(UUID applicationId) {
        jpa.deleteTagsByApplicationId(applicationId);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public List<UUID> findIdsByApplicantId(UUID applicantId) {
        return jpa.findIdsByApplicantId(applicantId);
    }

    @Override
    public List<UUID> findIdsByProductId(UUID productId) {
        return jpa.findIdsByProductId(productId);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    // mapping helpers
    private Application toDomain(ApplicationEntity e) {
        Application d = new Application();
        d.setId(e.getId());
        d.setApplicantId(e.getApplicantId());
        d.setProductId(e.getProductId());
        d.setStatus(e.getStatus());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        d.setVersion(e.getVersion());
        d.setFiles(e.getFiles() != null ? new HashSet<>(e.getFiles()) : new HashSet<>());
        d.setTags(e.getTags() != null ? new HashSet<>(e.getTags()) : new HashSet<>());

        // history mapping - map history entities to domain history
        if (e.getHistory() != null && !e.getHistory().isEmpty()) {
            List<ApplicationHistory> hist = e.getHistory().stream().map(h -> {
                ApplicationHistory dh = new ApplicationHistory();
                dh.setId(h.getId());
                dh.setApplicationId(e.getId());
                dh.setOldStatus(h.getOldStatus());
                dh.setNewStatus(h.getNewStatus());
                dh.setChangedBy(h.getChangedBy());
                dh.setChangedAt(h.getChangedAt());
                return dh;
            }).collect(Collectors.toList());
            d.setHistory(hist);
        }
        return d;
    }

    private ApplicationEntity toEntity(Application d) {
        ApplicationEntity e = new ApplicationEntity();
        e.setId(d.getId());
        e.setApplicantId(d.getApplicantId());
        e.setProductId(d.getProductId());
        e.setStatus(d.getStatus() != null ? d.getStatus() : null);
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        e.setVersion(d.getVersion());
        e.setFiles(d.getFiles() != null ? new HashSet<>(d.getFiles()) : new HashSet<>());
        e.setTags(d.getTags() != null ? new HashSet<>(d.getTags()) : new HashSet<>());

        if (d.getHistory() != null && !d.getHistory().isEmpty()) {
            List<ApplicationHistoryEntity> histEntities = d.getHistory().stream().map(h -> {
                ApplicationHistoryEntity he = new ApplicationHistoryEntity();
                he.setId(h.getId());
                he.setOldStatus(h.getOldStatus());
                he.setNewStatus(h.getNewStatus());
                he.setChangedBy(h.getChangedBy());
                he.setChangedAt(h.getChangedAt());
                // set application later by JPA cascade handling (we set manually below)
                he.setApplication(e);
                return he;
            }).collect(Collectors.toList());
            e.setHistory(histEntities);
        }
        return e;
    }

    private ApplicationHistoryEntity toHistoryEntity(ApplicationHistory h, ApplicationEntity parent) {
        ApplicationHistoryEntity he = new ApplicationHistoryEntity();
        he.setId(h.getId());
        he.setApplication(parent);
        he.setOldStatus(h.getOldStatus());
        he.setNewStatus(h.getNewStatus());
        he.setChangedBy(h.getChangedBy());
        he.setChangedAt(h.getChangedAt());
        return he;
    }
}
