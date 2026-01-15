package com.example.fileservice.service;

import com.example.fileservice.dto.FileMetadataResponse;
import com.example.fileservice.dto.FileUploadResponse;
import com.example.fileservice.exception.FileNotFoundException;
import com.example.fileservice.exception.FileStorageException;
import com.example.fileservice.model.entity.FileMetadata;
import com.example.fileservice.model.enums.FileStorageType;
import com.example.fileservice.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileMetadataRepository fileMetadataRepository;
    private final StorageService storageService;

    public FileService(FileMetadataRepository fileMetadataRepository, StorageService storageService) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.storageService = storageService;
    }

    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, UUID uploadedBy) {
        try {
            // Валидация файла
            validateFile(file);

            // Сохраняем файл в хранилище
            String storageKey = storageService.storeFile(file, file.getOriginalFilename());

            // Сохраняем метаданные в БД
            FileMetadata metadata = new FileMetadata();
            metadata.setId(UUID.randomUUID());
            metadata.setFileName(file.getOriginalFilename());
            metadata.setContentType(file.getContentType());
            metadata.setFileSize(file.getSize());
            metadata.setStorageKey(storageKey);
            metadata.setStorageType(FileStorageType.LOCAL);
            metadata.setUploadedBy(uploadedBy);
            metadata.setUploadedAt(Instant.now());

            fileMetadataRepository.save(metadata);

            log.info("File uploaded successfully: {}, uploadedBy: {}", metadata.getId(), uploadedBy);

            FileUploadResponse response = new FileUploadResponse();
            response.setId(metadata.getId());
            response.setFileName(metadata.getFileName());
            response.setContentType(metadata.getContentType());
            response.setFileSize(metadata.getFileSize());
            response.setUploadedBy(metadata.getUploadedBy());
            response.setUploadedAt(metadata.getUploadedAt());
            response.setDownloadUrl(storageService.getFileUrl(storageKey));
            return response;

        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public FileMetadataResponse getFileMetadata(UUID fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        return toResponse(metadata);
    }

    @Transactional(readOnly = true)
    public byte[] downloadFile(UUID fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        try (var inputStream = storageService.getFile(metadata.getStorageKey())) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new FileStorageException("Failed to download file: " + fileId, e);
        }
    }

    @Transactional
    public void attachFilesToApplication(UUID applicationId, List<UUID> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        fileMetadataRepository.attachFilesToApplication(fileIds, applicationId);
        log.info("Attached {} files to application: {}", fileIds.size(), applicationId);
    }

    @Transactional(readOnly = true)
    public List<FileMetadataResponse> getFilesByApplication(UUID applicationId) {
        List<FileMetadata> files = fileMetadataRepository.findByApplicationIdAndDeletedAtIsNull(applicationId);

        return files.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFile(UUID fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        // Мягкое удаление
        metadata.setDeletedAt(Instant.now());
        fileMetadataRepository.save(metadata);

        // Физическое удаление файла (можно сделать асинхронно)
        try {
            storageService.deleteFile(metadata.getStorageKey());
        } catch (Exception e) {
            log.warn("Failed to delete physical file: {}", metadata.getStorageKey(), e);
        }

        log.info("File deleted: {}", fileId);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new FileStorageException("File size exceeds limit (10MB)");
        }

        // Проверка MIME типов
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/") && !contentType.equals("application/pdf")) {
            throw new FileStorageException("Unsupported file type. Only images and PDF are allowed");
        }
    }

    private FileMetadataResponse toResponse(FileMetadata metadata) {
        FileMetadataResponse response = new FileMetadataResponse();
        response.setId(metadata.getId());
        response.setFileName(metadata.getFileName());
        response.setContentType(metadata.getContentType());
        response.setFileSize(metadata.getFileSize());
        response.setUploadedBy(metadata.getUploadedBy());
        response.setUploadedAt(metadata.getUploadedAt());
        response.setApplicationId(metadata.getApplicationId());
        response.setDownloadUrl(storageService.getFileUrl(metadata.getStorageKey()));
        return response;
    }
}