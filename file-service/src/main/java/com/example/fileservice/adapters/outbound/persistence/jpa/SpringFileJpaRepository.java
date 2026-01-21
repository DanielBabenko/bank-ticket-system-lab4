package com.example.fileservice.adapters.outbound.persistence.jpa;

import com.example.fileservice.adapters.outbound.persistence.entity.FileJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringFileJpaRepository extends JpaRepository<FileJpaEntity, UUID> {
    List<FileJpaEntity> findByIdIn(List<UUID> ids);
    Page<FileJpaEntity> findAll(Pageable pageable);
}
