package com.example.applicationservice.adapters.outbound.persistence.jpa;

import com.example.applicationservice.adapters.outbound.persistence.entity.ApplicationHistoryEntity;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ApplicationHistoryJpaRepository extends JpaRepository<ApplicationHistoryEntity, UUID> {
    List<ApplicationHistory> findByApplicationIdOrderByChangedAtDesc(UUID applicationId);
    @Modifying
    @Transactional
    @Query("DELETE FROM ApplicationHistory h WHERE h.application.id = :applicationId")
    void deleteByApplicationId(@Param("applicationId") UUID applicationId);
}