package com.example.fileservice.application.usecase;

import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import com.example.fileservice.domain.port.inbound.DeleteFileUseCasePort;
import com.example.fileservice.application.exception.ForbiddenException;
import com.example.fileservice.application.exception.NotFoundException;

import java.util.UUID;

public class DeleteFileUseCase implements DeleteFileUseCasePort {

    private final FileRepositoryPort fileRepository;
    private final StoragePort storagePort;

    public DeleteFileUseCase(FileRepositoryPort fileRepository, StoragePort storagePort) {
        this.fileRepository = fileRepository;
        this.storagePort = storagePort;
    }

    @Override
    public void deleteFile(UUID fileId, UUID userId, boolean isAdmin) {
        if (fileId == null) throw new IllegalArgumentException("File id required");
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        if (!(file.getUploaderId().equals(userId) || isAdmin)) {
            throw new ForbiddenException("You must be uploader or admin to delete this file");
        }

        try {
            storagePort.delete(file.getBucketName(), file.getStorageKey());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from storage", e);
        }

        fileRepository.delete(file);
    }
}
