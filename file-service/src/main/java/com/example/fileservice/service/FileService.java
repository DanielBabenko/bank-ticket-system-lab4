package com.example.fileservice.service;

import com.example.fileservice.dto.ApplicationInfoDto;
import com.example.fileservice.dto.FileDto;
import com.example.fileservice.exception.*;
import com.example.fileservice.feign.ApplicationServiceClient;
import com.example.fileservice.model.entity.File;
import com.example.fileservice.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private final FileRepository fileRepository;
    private final ApplicationServiceClient applicationServiceClient;
    private final MinioService minioService;

    public FileService(FileRepository fileRepository,
                       ApplicationServiceClient applicationServiceClient,
                       MinioService minioService) {
        this.fileRepository = fileRepository;
        this.applicationServiceClient = applicationServiceClient;
        this.minioService = minioService;
    }

    @Transactional
    public FileDto uploadFile(MultipartFile multipartFile, UUID uploaderId, String description) {
        // Валидация файла
        if (multipartFile.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BadRequestException("File name is required");
        }

        // Генерируем UUID для файла
        UUID fileId = UUID.randomUUID();

        // Создаем сущность файла
        File file = new File(
                fileId,
                originalFilename,
                multipartFile.getSize(),
                multipartFile.getContentType(),
                uploaderId
        );
        file.setDescription(description);

        try {
            // Загружаем файл в MinIO
            minioService.uploadFile(multipartFile, file.getStorageKey());

            // Сохраняем метаданные в БД
            fileRepository.save(file);

            log.info("File uploaded successfully: {} ({} bytes) by user {}",
                    originalFilename, multipartFile.getSize(), uploaderId);

            // Конвертируем в DTO
            return toDto(file);

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process file upload", e);
        }
    }

    public InputStreamWithMetadata downloadFile(UUID fileId, UUID userId, Jwt jwt) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        // Проверяем, существует ли файл в хранилище
        if (!minioService.fileExists(file.getStorageKey())) {
            throw new NotFoundException("File not found in storage: " + fileId);
        }

        boolean isAdminOrManager = false;
        if (jwt != null) {
            Object roleClaim = jwt.getClaims().get("role");
            if (roleClaim != null && (("ROLE_ADMIN".equals(roleClaim.toString())) || ("ROLE_MANAGER".equals(roleClaim.toString())))) {
                isAdminOrManager = true;
            }
        }

        if (!(file.getUploaderId().equals(userId) || isAdminOrManager)) {
            throw new ForbiddenException("You must be uploader, manager or admin to download this file");
        }

        // Получаем поток файла
        InputStream fileStream = minioService.downloadFile(file.getStorageKey());

        return new InputStreamWithMetadata(
                fileStream,
                file.getOriginalName(),
                file.getMimeType(),
                file.getSize()
        );
    }

    @Transactional
    public void deleteFile(UUID fileId, UUID userId, Jwt jwt) {
        if (fileId == null) {
            throw new BadRequestException("File ID is required");
        }

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        boolean isAdmin = false;
        if (jwt != null) {
            Object roleClaim = jwt.getClaims().get("role");
            if (roleClaim != null && (("ROLE_ADMIN".equals(roleClaim.toString())))) {
                isAdmin = true;
            }
        }

        if (!(file.getUploaderId().equals(userId) || isAdmin)) {
            throw new ForbiddenException("You must be uploader or admin to delete this file");
        }

        try {
            minioService.deleteFile(file.getStorageKey());
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", file.getStorageKey(), e);
            throw new BadRequestException("Failed to delete file from MinIO");
        }

        fileRepository.delete(file);

        log.info("File deleted successfully: {} (ID: {}) by user {}",
                file.getOriginalName(), fileId, userId);
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
    public List<UUID> getFilesBatch(List<UUID> fileIds) {
        List<File> files = getFiles(fileIds);
        return files.stream()
                .map(File::getId)
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
            dto.setOriginalName(file.getOriginalName());
            dto.setMimeType(file.getMimeType());
            dto.setSize(file.getSize());
            dto.setExtension(file.getExtension());
            dto.setUploadDate(file.getUploadDate());
            dto.setUploaderId(file.getUploaderId());
            dto.setDescription(file.getDescription());
            dto.setDownloadUrl(minioService.getFileUrl(file.getStorageKey()));
            dto.setApplications(applications);

            return dto;
        } catch (Exception e) {
            log.error("Error fetching applications for file {}: {}", file.getOriginalName(), e.getMessage());

            FileDto dto = new FileDto();
            dto.setId(file.getId());
            dto.setOriginalName(file.getOriginalName());
            dto.setMimeType(file.getMimeType());
            dto.setSize(file.getSize());
            dto.setExtension(file.getExtension());
            dto.setUploadDate(file.getUploadDate());
            dto.setUploaderId(file.getUploaderId());
            dto.setDescription(file.getDescription());
            dto.setDownloadUrl(minioService.getFileUrl(file.getStorageKey()));
            dto.setApplications(Collections.emptyList());

            return dto;
        }
    }

    // Вспомогательный класс для возврата файла с метаданными
        public record InputStreamWithMetadata(InputStream inputStream, String filename, String contentType, Long size) {
    }
}
