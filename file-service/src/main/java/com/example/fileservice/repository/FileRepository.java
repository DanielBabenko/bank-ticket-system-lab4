// FileRepository.java (дополненный)
package com.example.fileservice.repository;

import com.example.fileservice.model.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {

    @Query("SELECT f FROM File f WHERE f.id IN :ids")
    List<File> findByIds(@Param("ids") List<UUID> ids);

}