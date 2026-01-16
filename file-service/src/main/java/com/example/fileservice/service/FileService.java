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
    public FileDto uploadFile(MultipartFile multipartFile, UUID uploaderId, String uploaderUsername, String description) {
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
                uploaderId,
                uploaderUsername
        );
        file.setDescription(description);

        try {
            // Загружаем файл в MinIO
            minioService.uploadFile(multipartFile, file.getStorageKey());

            // Сохраняем метаданные в БД
            fileRepository.save(file);

            log.info("File uploaded successfully: {} ({} bytes) by user {}",
                    originalFilename, multipartFile.getSize(), uploaderUsername);

            // Конвертируем в DTO
            return toDto(file);

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process file upload", e);
        }
    }

    public InputStreamWithMetadata downloadFile(UUID fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        // Проверяем, существует ли файл в хранилище
        if (!minioService.fileExists(file.getStorageKey())) {
            throw new NotFoundException("File not found in storage: " + fileId);
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
                .map(this::toDto)
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
            dto.setUploaderUsername(file.getUploaderUsername());
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
            dto.setUploaderUsername(file.getUploaderUsername());
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
