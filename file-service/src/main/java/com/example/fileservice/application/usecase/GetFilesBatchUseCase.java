package com.example.fileservice.application.usecase;

import com.example.fileservice.domain.port.inbound.GetFilesBatchUseCasePort;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetFilesBatchUseCase implements GetFilesBatchUseCasePort {

    private final FileRepositoryPort repository;

    public GetFilesBatchUseCase(FileRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<UUID> getFilesBatch(List<UUID> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) return List.of();
        var files = repository.findByIds(fileIds);
        return files.stream().map(com.example.fileservice.domain.model.File::getId).collect(Collectors.toList());
    }
}
