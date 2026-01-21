package com.example.fileservice.adapters.outbound.persistence;

import com.example.fileservice.adapters.outbound.persistence.entity.FileJpaEntity;
import com.example.fileservice.adapters.outbound.persistence.jpa.SpringFileJpaRepository;
import com.example.fileservice.domain.model.File;
import com.example.fileservice.domain.port.outbound.FileRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FileRepositoryAdapter implements FileRepositoryPort {

    private final SpringFileJpaRepository jpaRepository;

    public FileRepositoryAdapter(SpringFileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<File> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional
    public File save(File file) {
        FileJpaEntity entity = toEntity(file);
        FileJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void delete(File file) {
        jpaRepository.deleteById(file.getId());
    }

    @Override
    public List<File> findByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return jpaRepository.findByIdIn(ids).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<File> findAll(int page, int size) {
        var pr = PageRequest.of(page, size);
        return jpaRepository.findAll(pr).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    private File toDomain(FileJpaEntity e) {
        File f = new File();
        f.setId(e.getId());
        f.setOriginalName(e.getOriginalName());
        f.setStorageKey(e.getStorageKey());
        f.setMimeType(e.getMimeType());
        f.setSize(e.getSize());
        f.setExtension(e.getExtension());
        f.setUploadDate(e.getUploadDate());
        f.setUploaderId(e.getUploaderId());
        f.setBucketName(e.getBucketName());
        f.setDescription(e.getDescription());
        f.setApplicationIds(e.getApplicationIds());
        return f;
    }

    private FileJpaEntity toEntity(File f) {
        FileJpaEntity e = new FileJpaEntity();
        e.setId(f.getId());
        e.setOriginalName(f.getOriginalName());
        e.setStorageKey(f.getStorageKey());
        e.setMimeType(f.getMimeType());
        e.setSize(f.getSize());
        e.setExtension(f.getExtension());
        e.setUploadDate(f.getUploadDate());
        e.setUploaderId(f.getUploaderId());
        e.setBucketName(f.getBucketName());
        e.setDescription(f.getDescription());
        e.setApplicationIds(f.getApplicationIds());
        return e;
    }
}
