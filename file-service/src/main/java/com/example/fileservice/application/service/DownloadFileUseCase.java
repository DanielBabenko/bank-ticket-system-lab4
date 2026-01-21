package com.example.fileservice.application.service;

import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.inbound.DownloadFileUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import com.example.fileservice.application.exception.ForbiddenException;
import com.example.fileservice.application.exception.NotFoundException;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public class DownloadFileUseCase implements DownloadFileUseCasePort {

    private final FileRepositoryPort fileRepository;
    private final StoragePort storagePort;

    public DownloadFileUseCase(FileRepositoryPort fileRepository, StoragePort storagePort) {
        this.fileRepository = fileRepository;
        this.storagePort = storagePort;
    }

    @Override
    public DownloadResult downloadFile(UUID fileId, UUID userId, Object jwt) {
        if (fileId == null) throw new IllegalArgumentException("File id required");
        var opt = fileRepository.findById(fileId);
        File file = opt.orElseThrow(() -> new NotFoundException("File not found: " + fileId));

        boolean isAdminOrManager = false;
        if (jwt != null) {
            // jwt is framework object (adapter). Here we accept Object; adapters pass Jwt and we inspect in decorator if needed.
            // For pure application, we don't parse JWT; assume adapter validated roles and passed userId accordingly.
        }

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
