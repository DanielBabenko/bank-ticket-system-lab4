package com.example.productservice.adapters.outbound.persistence.jpa;

import com.example.productservice.adapters.outbound.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    boolean existsByName(String name);
}
