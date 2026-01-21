package com.example.fileservice.application.usecase;

import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.inbound.DownloadFileUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import com.example.fileservice.application.exception.ForbiddenException;
import com.example.fileservice.application.exception.NotFoundException;

import java.io.InputStream;
import java.util.UUID;

public class DownloadFileUseCase implements DownloadFileUseCasePort {

    private final FileRepositoryPort fileRepository;
    private final StoragePort storagePort;

    public DownloadFileUseCase(FileRepositoryPort fileRepository, StoragePort storagePort) {
        this.fileRepository = fileRepository;
        this.storagePort = storagePort;
    }

    @Override
    public DownloadResult downloadFile(UUID fileId, UUID userId, boolean isAdminOrManager) {
        if (fileId == null) throw new IllegalArgumentException("File id required");
        var opt = fileRepository.findById(fileId);
        File file = opt.orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        if (!(file.getUploaderId().equals(userId) || isAdminOrManager)) {
            throw new ForbiddenException("You must be uploader, manager or admin to download this file");
        }

        try {
            InputStream stream = storagePort.download(file.getBucketName(), file.getStorageKey());
            return new DownloadResult(stream, file.getOriginalName(), file.getMimeType(), file.getSize());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }
}
