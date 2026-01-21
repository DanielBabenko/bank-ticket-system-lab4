package com.example.fileservice.application.usecase;

import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.inbound.GetFileUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import com.example.fileservice.application.exception.NotFoundException;

import java.util.UUID;

public class GetFileUseCase implements GetFileUseCasePort {

    private final FileRepositoryPort repository;

    public GetFileUseCase(FileRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public File getFileById(UUID id) {
        if (id == null) throw new IllegalArgumentException("File id required");
        return repository.findById(id).orElseThrow(() -> new NotFoundException("File not found: " + id));
    }
}
