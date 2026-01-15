package com.example.fileservice.service;

import com.example.fileservice.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    @Value("${file.storage.local.directory:./uploads}")
    private String storageDirectory;

    @Override
    public String storeFile(MultipartFile file, String fileName) {
        try {
            // Создаем уникальное имя файла
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;
            Path storagePath = Paths.get(storageDirectory).resolve(uniqueFileName);

            // Создаем директорию если не существует
            Files.createDirectories(storagePath.getParent());

            // Сохраняем файл
            Files.copy(file.getInputStream(), storagePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved locally: {}", storagePath);
            return storagePath.toString();

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public InputStream getFile(String storageKey) {
        try {
            return Files.newInputStream(Paths.get(storageKey));
        } catch (IOException e) {
            throw new FileStorageException("Failed to read file: " + storageKey, e);
        }
    }

    @Override
    public void deleteFile(String storageKey) {
        try {
            Files.deleteIfExists(Paths.get(storageKey));
            log.info("File deleted: {}", storageKey);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + storageKey, e);
        }
    }

    @Override
    public String getFileUrl(String storageKey) {
        // Для локального хранилища возвращаем путь для API
        Path path = Paths.get(storageKey);
        String fileName = path.getFileName().toString();
        return "/api/v1/files/download/" + fileName;
    }
}
