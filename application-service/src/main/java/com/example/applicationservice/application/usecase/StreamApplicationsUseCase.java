package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.BadRequestException;
import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.port.inbound.StreamApplicationsUseCasePort;
import com.example.applicationservice.domain.port.outbound.ApplicationRepositoryPort;
import com.example.applicationservice.domain.port.outbound.FileServicePort;
import com.example.applicationservice.domain.util.ApplicationPage;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.application.util.CursorUtil;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class StreamApplicationsUseCase implements StreamApplicationsUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final FileServicePort fileService;

    public StreamApplicationsUseCase(ApplicationRepositoryPort applicationRepository,
                                     FileServicePort fileService) {
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
            CursorUtil.Decoded decoded = CursorUtil.decode(cursor);
            if (decoded == null) {
                throw new BadRequestException("Invalid cursor");
            }
            ts = decoded.timestamp;
            id = decoded.id;
        }

        List<UUID> appIds;
        if (ts == null) {
            appIds = applicationRepository.findIdsFirstPage(capped);
        } else {
            appIds = applicationRepository.findIdsByKeyset(ts, id, capped);
        }

        if (appIds == null || appIds.isEmpty()) {
            return new ApplicationPage(List.of(), null);
        }

        List<Application> appsWithFiles = applicationRepository.findByIdsWithFiles(appIds);
        List<Application> appsWithTags = applicationRepository.findByIdsWithTags(appIds);

        Map<UUID, Application> map = new HashMap<>();
        for (Application a : appsWithFiles) map.put(a.getId(), a);
        for (Application a : appsWithTags) {
            Application exist = map.get(a.getId());
            if (exist != null) exist.setTags(a.getTags());
        }

        // gather all file ids
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
                // If file service fails, mark all files as existing (backwards-compatible choice)
                for (UUID f : allFileIds) existingFiles.put(f, true);
            }
        }

        // filter files per application
        for (Application a : map.values()) {
            if (a.getFiles() != null) {
                Set<UUID> filtered = a.getFiles().stream()
                        .filter(fid -> existingFiles.getOrDefault(fid, true))
                        .collect(Collectors.toSet());
                a.setFiles(filtered);
            }
        }

        List<Application> ordered = appIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());

        String nextCursor = null;
        if (!ordered.isEmpty()) {
            Application last = ordered.get(ordered.size() - 1);
            nextCursor = CursorUtil.encode(last.getCreatedAt(), last.getId());
        }

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
