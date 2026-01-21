package com.example.fileservice.application.usecase;

import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.inbound.ListFilesUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;

import java.util.List;

public class ListFilesUseCase implements ListFilesUseCasePort {

    private final FileRepositoryPort repository;

    public ListFilesUseCase(FileRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<File> listAll(int page, int size) {
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
        return repository.findAll(page, size);
    }
}
