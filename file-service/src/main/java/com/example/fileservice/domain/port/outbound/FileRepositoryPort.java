package com.example.fileservice.domain.port.outbound;

import com.example.fileservice.domain.model.File;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepositoryPort {
    Optional<File> findById(UUID id);
    File save(File file);
    void delete(File file);
    List<File> findByIds(List<UUID> ids);
    List<File> findAll(int page, int size);
    boolean existsById(UUID id);
}
