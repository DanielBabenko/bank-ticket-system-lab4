package com.example.applicationservice.adapters.outbound.persistence;

import com.example.applicationservice.adapters.outbound.persistence.entity.ApplicationEntity;
import com.example.applicationservice.adapters.outbound.persistence.entity.ApplicationHistoryEntity;
import com.example.applicationservice.adapters.outbound.persistence.jpa.ApplicationHistoryJpaRepository;
import com.example.applicationservice.adapters.outbound.persistence.jpa.ApplicationJpaRepository;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.port.outbound.ApplicationHistoryRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ApplicationHistoryRepositoryAdapter implements ApplicationHistoryRepositoryPort {

    private final ApplicationHistoryJpaRepository jpa;
    private final ApplicationJpaRepository applicationJpa;

    public ApplicationHistoryRepositoryAdapter(ApplicationHistoryJpaRepository jpa, ApplicationJpaRepository applicationJpa) {
        this.jpa = jpa;
        this.applicationJpa = applicationJpa;
    }

    @Override
    public ApplicationHistory save(ApplicationHistory history) {
        ApplicationHistoryEntity e = new ApplicationHistoryEntity();
        e.setId(history.getId());
        // ====== ВАЖНО: установить parent entity ======
        if (history.getApplicationId() != null) {
            // Лучше использовать getReferenceById (не делает select), но можно и findById
            ApplicationEntity parent = applicationJpa.getReferenceById(history.getApplicationId());
            e.setApplication(parent);
        }
        e.setOldStatus(history.getOldStatus());
        e.setNewStatus(history.getNewStatus());
        e.setChangedBy(history.getChangedBy());
        e.setChangedAt(history.getChangedAt());

        ApplicationHistoryEntity saved = jpa.save(e);

        ApplicationHistory dh = new ApplicationHistory();
        dh.setId(saved.getId());
        dh.setApplicationId(saved.getApplication() != null ? saved.getApplication().getId() : null);
        dh.setOldStatus(saved.getOldStatus());
        dh.setNewStatus(saved.getNewStatus());
        dh.setChangedBy(saved.getChangedBy());
        dh.setChangedAt(saved.getChangedAt());
        return dh;
    }

    @Override
    public List<ApplicationHistory> findByApplicationIdOrderByChangedAtDesc(UUID applicationId) {
        return jpa.findByApplicationIdOrderByChangedAtDesc(applicationId).stream().map(e -> {
            ApplicationHistory dh = new ApplicationHistory();
            dh.setId(e.getId());
            dh.setApplicationId(e.getApplication().getId());
            dh.setOldStatus(e.getOldStatus());
            dh.setNewStatus(e.getNewStatus());
            dh.setChangedBy(e.getChangedBy());
            dh.setChangedAt(e.getChangedAt());
            return dh;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteByApplicationId(UUID applicationId) {
        jpa.deleteByApplicationId(applicationId);
    }
}
