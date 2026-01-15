package com.example.fileservice.model.entity;

import com.example.fileservice.model.enums.FileStorageType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String storageKey; // Путь в S3 или локальной файловой системе

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStorageType storageType;

    @Column(nullable = false)
    private UUID uploadedBy;

    @Column
    private UUID applicationId; // Привязка к заявке (может быть null)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column
    private Instant deletedAt;

    @Version
    private Long version;
}
