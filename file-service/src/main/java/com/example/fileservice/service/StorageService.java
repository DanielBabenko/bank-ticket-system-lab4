package com.example.fileservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String storeFile(MultipartFile file, String fileName);
    InputStream getFile(String storageKey);
    void deleteFile(String storageKey);
    String getFileUrl(String storageKey);
}