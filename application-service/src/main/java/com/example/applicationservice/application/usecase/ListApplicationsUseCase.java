package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.*;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.port.outbound.*;
import com.example.applicationservice.domain.port.inbound.ListApplicationsUseCasePort;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Возвращает list<Application> для данного page/size.
 * Логика фильтрации не-существующих файлов — сохраняем поведение: при ошибке file service считаем все файлы существующими.
 */
public class ListApplicationsUseCase implements ListApplicationsUseCasePort {

    private final ApplicationRepositoryPort applicationRepository;
    private final FileServicePort fileService;

    public ListApplicationsUseCase(ApplicationRepositoryPort applicationRepository, FileServicePort fileService) {
        this.applicationRepository = applicationRepository;
        this.fileService = fileService;
    }

    @Override
    public List<Application> listApplications(int page, int size) {
        if (size > 50) throw new BadRequestException("Page size cannot exceed 50");

        List<Application> applications = applicationRepository.findAll(page, size);
        if (applications == null || applications.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = applications.stream().map(Application::getId).collect(Collectors.toList());
        List<Application> withTags = applicationRepository.findByIdsWithTags(ids);
        Map<UUID, Set<String>> tagsMap = new HashMap<>();
        for (Application a : withTags) tagsMap.put(a.getId(), a.getTags());

        List<Application> withFiles = applicationRepository.findByIdsWithFiles(ids);
        Map<UUID, Set<UUID>> filesMap = new HashMap<>();
        for (Application a : withFiles) filesMap.put(a.getId(), a.getFiles());

        // check file existence
        Set<UUID> allFileIds = filesMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        Map<UUID, Boolean> existingFiles = new HashMap<>();
        if (!allFileIds.isEmpty()) {
            try {
                List<UUID> existing = fileService.checkFilesExist(new ArrayList<>(allFileIds));
                Set<UUID> existingSet = existing != null ? new HashSet<>(existing) : Collections.emptySet();
                for (UUID fid : allFileIds) {
                    existingFiles.put(fid, existingSet.contains(fid));
                }
            } catch (Exception e) {
                // on failure assume files exist
                for (UUID fid : allFileIds) existingFiles.put(fid, true);
            }
        }

        // assign filtered files and tags
        for (Application app : applications) {
            Set<String> tags = tagsMap.get(app.getId());
            if (tags != null) app.setTags(tags);
            Set<UUID> files = filesMap.get(app.getId());
            if (files != null) {
                Set<UUID> filtered = files.stream().filter(fid -> existingFiles.getOrDefault(fid, true)).collect(Collectors.toSet());
                app.setFiles(filtered);
            }
        }

        return applications;
    }
}
