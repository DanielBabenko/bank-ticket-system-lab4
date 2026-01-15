package com.example.fileservice.repository;

import com.example.fileservice.model.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    @Query("SELECT f FROM FileEntity f WHERE :applicationId MEMBER OF f.applicationIds")
    List<FileEntity> findByApplicationId(@Param("applicationId") UUID applicationId);

    boolean existsByIdAndUploadedBy(UUID id, UUID uploadedBy);
}