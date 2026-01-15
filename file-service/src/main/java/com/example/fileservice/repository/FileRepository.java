package com.example.fileservice.repository;

import com.example.fileservice.model.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {

    Optional<File> findByName(String name);

    @Query("SELECT f FROM File f WHERE f.name IN :names")
    List<File> findByNames(@Param("names") List<String> names);

    @Query("SELECT f FROM File f WHERE f.id IN :ids")
    List<File> findByIds(@Param("ids") List<UUID> ids);

    boolean existsByName(String name);
}
