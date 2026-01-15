package com.example.fileservice.repository;

import com.example.fileservice.model.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    List<FileMetadata> findByApplicationId(UUID applicationId);

    List<FileMetadata> findByUploadedBy(UUID uploadedBy);

    List<FileMetadata> findByApplicationIdAndDeletedAtIsNull(UUID applicationId);

    @Modifying
    @Query("UPDATE FileMetadata f SET f.deletedAt = :deletedAt WHERE f.id IN :fileIds")
    void softDeleteFiles(@Param("fileIds") List<UUID> fileIds, @Param("deletedAt") Instant deletedAt);

    @Modifying
    @Query("UPDATE FileMetadata f SET f.applicationId = :applicationId WHERE f.id IN :fileIds")
    void attachFilesToApplication(
            @Param("fileIds") List<UUID> fileIds,
            @Param("applicationId") UUID applicationId
    );
}