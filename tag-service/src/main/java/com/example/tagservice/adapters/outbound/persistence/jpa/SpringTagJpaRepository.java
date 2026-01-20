package com.example.tagservice.adapters.outbound.persistence.jpa;

import com.example.tagservice.adapters.outbound.persistence.entity.TagJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringTagJpaRepository extends JpaRepository<TagJpaEntity, UUID> {
    Optional<TagJpaEntity> findByName(String name);
    List<TagJpaEntity> findByNameIn(List<String> names);
    Page<TagJpaEntity> findAll(Pageable pageable);
    void deleteAll();
    long count();
}
