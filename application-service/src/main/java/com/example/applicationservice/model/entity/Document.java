package com.example.applicationservice.model.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "document")
public class Document {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID fileId; // Единственная связь с File-Service

    @Column
    private String description; // Опционально, для внутреннего использования

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Transient
    private FileMetadata metadata;
}