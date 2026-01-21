package com.example.applicationservice.domain.port.outbound;

import com.example.applicationservice.domain.model.entity.Application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepositoryPort {

    Application save(Application application);

    Optional<Application> findById(UUID id);

    Optional<Application> findByIdWithFiles(UUID id);

    Optional<Application> findByIdWithTags(UUID id);

    List<Application> findAll(int page, int size);

    List<UUID> findIdsFirstPage(int limit);

    List<UUID> findIdsByKeyset(Instant timestamp, UUID id, int limit);

    List<Application> findByIdsWithTags(List<UUID> ids);

    List<Application> findByIdsWithFiles(List<UUID> ids);

    List<Application> findByTag(String tagName);

    List<Application> findByFile(UUID fileId);

    void deleteFilesByApplicationId(UUID applicationId);

    void deleteTagsByApplicationId(UUID applicationId);

    void deleteById(UUID id);

    List<UUID> findIdsByApplicantId(UUID applicantId);

    List<UUID> findIdsByProductId(UUID productId);

    long count();
}
