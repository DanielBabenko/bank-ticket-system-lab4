package com.example.fileservice.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface StorageService {
    String store(MultipartFile file, UUID fileId);
    Resource loadAsResource(String filename);
    void delete(String filename);
    Path getStorageLocation();
}