package com.example.fileservice.application.usecase;

import com.example.fileservice.application.command.UploadFileCommand;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.domain.port.outbound.StoragePort;
import com.example.fileservice.domain.port.inbound.UploadFileUseCasePort;

public class UploadFileUseCase implements UploadFileUseCasePort {

    private final FileRepositoryPort fileRepository;
    private final StoragePort storagePort;

    public UploadFileUseCase(FileRepositoryPort fileRepository, StoragePort storagePort) {
        this.fileRepository = fileRepository;
        this.storagePort = storagePort;
    }

    @Override
    public File uploadFile(UploadFileCommand command) {
        if (command == null) throw new IllegalArgumentException("Command is required");
        if (command.getInputStream() == null) throw new IllegalArgumentException("Input stream is required");
        if (command.getOriginalName() == null || command.getOriginalName().trim().isEmpty())
            throw new IllegalArgumentException("Original name required");

        File file = File.createNew(command.getOriginalName(), command.getSize(), command.getContentType(), command.getUploaderId());
        file.setDescription(command.getDescription());

        try {
            // Сначала загружаем в хранилище
            storagePort.upload(file.getBucketName(), file.getStorageKey(), command.getInputStream(), command.getSize(), command.getContentType());

            // Затем сохраняем метаданные
            File saved = fileRepository.save(file);
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
}
