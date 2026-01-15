package com.example.fileservice.service;

import com.example.fileservice.dto.ApplicationInfoDto;
import com.example.fileservice.dto.FileDto;
import com.example.fileservice.exception.BadRequestException;
import com.example.fileservice.exception.NotFoundException;
import com.example.fileservice.exception.ServiceUnavailableException;
import com.example.fileservice.feign.ApplicationServiceClient;
import com.example.fileservice.model.entity.File;
import com.example.fileservice.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FileService {
    
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private final FileRepository fileRepository;
    private final ApplicationServiceClient applicationServiceClient;

    public FileService(FileRepository fileRepository, ApplicationServiceClient applicationServiceClient) {
        this.fileRepository = fileRepository;
        this.applicationServiceClient = applicationServiceClient;
    }

    @Transactional
    public File createIfNotExists(String name) {
        return fileRepository.findByName(name)
                .orElseGet(() -> {
                    File file = new File();
                    file.setId(UUID.randomUUID());
                    file.setName(name.trim());
                    File saved = fileRepository.save(file);
                    log.info("Created new file: {}", name);
                    return saved;
                });
    }

    @Transactional
    public List<File> getFiles(List<UUID> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> uniqueIds = fileIds.stream()
                .distinct()
                .collect(Collectors.toList());

        return fileRepository.findByIds(uniqueIds);
    }

    // Метод для совместимости с Feign (возвращает DTO)
    @Transactional
    public List<FileDto> getFilesBatch(List<UUID> fileIds) {
        List<File> files = getFiles(fileIds);
        return files.stream()
                .map(file -> {
                    FileDto dto = new FileDto();
                    dto.setId(file.getId());
                    dto.setName(file.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FileDto> listAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<File> files = fileRepository.findAll(pageable);

        return files.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public FileDto getFileById(UUID id) {
        if (id == null) {
            throw new BadRequestException("File name required");
        }
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found: " + id));
        return toDto(file);
    }

    private FileDto toDto(File file) {
        try {
            List<ApplicationInfoDto> applications = applicationServiceClient.getApplicationsByFile(file.getId());
            if (applications == null) {
                throw new ServiceUnavailableException("Application service is unavailable now");
            }
            FileDto dto = new FileDto();
            dto.setId(file.getId());
            dto.setName(file.getName());
            dto.setApplications(applications);
            return dto;
        } catch (Exception e) {
            log.error("Error fetching applications for file {}: {}", file.getName(), e.getMessage());
            FileDto dto = new FileDto();
            dto.setId(file.getId());
            dto.setName(file.getName());
            dto.setApplications(Collections.emptyList());
            return dto;
        }
    }
}
