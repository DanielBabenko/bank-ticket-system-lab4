package com.example.applicationservice.domain.port.outbound;

import com.example.applicationservice.domain.model.entity.ApplicationHistory;

import java.util.List;
import java.util.UUID;

public interface ApplicationHistoryRepositoryPort {

    ApplicationHistory save(ApplicationHistory history);

    List<ApplicationHistory> findByApplicationIdOrderByChangedAtDesc(UUID applicationId);

    void deleteByApplicationId(UUID applicationId);
}
