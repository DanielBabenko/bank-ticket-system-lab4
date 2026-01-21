package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.BadRequestException;
import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.port.inbound.StreamApplicationsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.util.ApplicationPage;
import com.example.applicationservice.domain.model.entity.Application;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация keyset-пагинации в синхронном стиле.
 */
public class StreamApplicationsUseCase implements StreamApplicationsUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final com.example.applicationservice.domain.port.outbound.FileServicePort fileService;

    public StreamApplicationsUseCase(ApplicationRepositoryPort applicationRepository,
                                     com.example.applicationservice.domain.port.outbound.FileServicePort fileService) {
        this.applicationRepository = applicationRepository;
        this.fileService = fileService;
    }

    @Override
    public ApplicationPage streamWithNextCursor(String cursor, int limit) {
        if (limit <= 0) throw new BadRequestException("limit must be greater than 0");
        int capped = Math.min(limit, 50);

        Instant ts = null;
        UUID id = null;
        if (cursor != null && !cursor.trim().isEmpty()) {
            // adapters should decode cursor to timestamp+id and call this method with decoded values,
            // but for backward compatibility keep cursor handling in adapters; here we expect cursor==null
            throw new BadRequestException("Cursor decoding must be handled by adapters");
        }

        List<UUID> appIds = applicationRepository.findIdsFirstPage(capped);
        if (appIds == null || appIds.isEmpty()) return new ApplicationPage(List.of(), null);

        List<Application> appsWithDocs = applicationRepository.findByIdsWithFiles(appIds);
        List<Application> appsWithTags = applicationRepository.findByIdsWithTags(appIds);

        Map<UUID, Application> map = new HashMap<>();
        for (Application a : appsWithDocs) map.put(a.getId(), a);
        for (Application a : appsWithTags) {
            Application exist = map.get(a.getId());
            if (exist != null) exist.setTags(a.getTags());
        }

        // gather file ids
        Set<UUID> allFileIds = map.values().stream()
                .map(Application::getFiles)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Map<UUID, Boolean> existingFiles = new HashMap<>();
        if (!allFileIds.isEmpty()) {
            try {
                List<UUID> existing = fileService.checkFilesExist(new ArrayList<>(allFileIds));
                Set<UUID> set = existing != null ? new HashSet<>(existing) : Collections.emptySet();
                for (UUID f : allFileIds) existingFiles.put(f, set.contains(f));
            } catch (Exception e) {
                for (UUID f : allFileIds) existingFiles.put(f, true);
            }
        }

        // filter files per application
        for (Application a : map.values()) {
            if (a.getFiles() != null) {
                Set<UUID> filtered = a.getFiles().stream().filter(fid -> existingFiles.getOrDefault(fid, true)).collect(Collectors.toSet());
                a.setFiles(filtered);
            }
        }

        List<Application> ordered = appIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
        String nextCursor = null;
        if (!ordered.isEmpty()) {
            Application last = ordered.get(ordered.size() - 1);
            nextCursor = java.util.Base64.getEncoder().encodeToString((last.getCreatedAt().toString() + "|" + last.getId().toString()).getBytes());
        }

        // map to ApplicationInfo domain DTOs
        List<ApplicationInfo> infos = ordered.stream().map(a -> {
            ApplicationInfo ai = new ApplicationInfo();
            ai.setId(a.getId());
            ai.setApplicantId(a.getApplicantId());
            ai.setProductId(a.getProductId());
            ai.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
            ai.setCreatedAt(a.getCreatedAt());
            return ai;
        }).collect(Collectors.toList());

        return new ApplicationPage(infos, nextCursor);
    }
}
