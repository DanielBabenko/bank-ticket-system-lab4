package com.example.fileservice.service;

import com.example.fileservice.dto.FileMetadataDto;
import com.example.fileservice.exception.FileNotFoundException;
import com.example.fileservice.exception.UnauthorizedException;
import com.example.fileservice.model.entity.FileEntity;
import com.example.fileservice.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final StorageService storageService;

    public FileService(FileRepository fileRepository, StorageService storageService) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
    }

    @Transactional
    public FileMetadataDto uploadFile(MultipartFile file, UUID userId, UUID applicationId) {
        UUID fileId = UUID.randomUUID();

        // Сохраняем файл в хранилище
        String storagePath = storageService.store(file, fileId);

        // Сохраняем метаданные в БД
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(fileId);
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setSize(file.getSize());
        fileEntity.setStoragePath(storagePath);
        fileEntity.setUploadedBy(userId);

        if (applicationId != null) {
            fileEntity.getApplicationIds().add(applicationId);
        }

        fileRepository.save(fileEntity);

        log.info("File uploaded: {} by user {}", fileId, userId);
        return toDto(fileEntity);
    }

    @Transactional(readOnly = true)
    public FileMetadataDto getFileMetadata(UUID fileId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        // Проверяем права доступа
        if (!fileEntity.getUploadedBy().equals(userId) &&
                fileEntity.getApplicationIds().isEmpty()) {
            throw new UnauthorizedException("No access to file");
        }

        return toDto(fileEntity);
    }

    @Transactional
    public void attachToApplication(UUID fileId, UUID applicationId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        // Проверяем, что пользователь - владелец файла
        if (!fileEntity.getUploadedBy().equals(userId)) {
            throw new UnauthorizedException("Only file owner can attach it to applications");
        }

        fileEntity.getApplicationIds().add(applicationId);
        fileRepository.save(fileEntity);

        log.info("File {} attached to application {}", fileId, applicationId);
    }

    @Transactional
    public void detachFromApplication(UUID fileId, UUID applicationId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        if (!fileEntity.getUploadedBy().equals(userId)) {
            throw new UnauthorizedException("Only file owner can detach it from applications");
        }

        boolean removed = fileEntity.getApplicationIds().remove(applicationId);
        fileRepository.save(fileEntity);

        if (removed) {
            log.info("File {} detached from application {}", fileId, applicationId);
        } else {
            log.info("File {} was not attached to application {}", fileId, applicationId);
        }
    }

    @Transactional(readOnly = true)
    public List<FileMetadataDto> getFilesByApplication(UUID applicationId) {
        List<FileEntity> files = fileRepository.findByApplicationId(applicationId);

        return files.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(UUID fileId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        // Проверяем права доступа
        if (!fileEntity.getUploadedBy().equals(userId) &&
                fileEntity.getApplicationIds().isEmpty()) {
            throw new UnauthorizedException("No access to file");
        }

        // Извлекаем имя файла из пути
        String filename = fileEntity.getStoragePath()
                .substring(fileEntity.getStoragePath().lastIndexOf("/") + 1);

        return storageService.loadAsResource(filename);
    }

    @Transactional
    public void deleteFile(UUID fileId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        // Проверяем права
        if (!fileEntity.getUploadedBy().equals(userId)) {
            throw new UnauthorizedException("Only file owner can delete it");
        }

        // Удаляем файл из хранилища
        String filename = fileEntity.getStoragePath()
                .substring(fileEntity.getStoragePath().lastIndexOf("/") + 1);
        storageService.delete(filename);

        // Удаляем метаданные
        fileRepository.delete(fileEntity);

        log.info("File deleted: {} by user {}", fileId, userId);
    }

    private FileMetadataDto toDto(FileEntity fileEntity) {
        FileMetadataDto metadata = new FileMetadataDto();
        metadata.setId(fileEntity.getId());
        metadata.setFileName(fileEntity.getFileName());
        metadata.setContentType(fileEntity.getContentType());
        metadata.setSize(fileEntity.getSize());
        metadata.setUploadedAt(fileEntity.getUploadedAt());
        metadata.setDownloadUrl("/api/v1/files/" + fileEntity.getId());

        return metadata;
    }
}