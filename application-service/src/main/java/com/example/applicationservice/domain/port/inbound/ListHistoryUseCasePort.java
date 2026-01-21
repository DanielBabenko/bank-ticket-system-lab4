package com.example.applicationservice.domain.port.inbound;

import com.example.applicationservice.domain.model.entity.ApplicationHistory;

import java.util.List;
import java.util.UUID;

public interface ListHistoryUseCasePort {
    List<ApplicationHistory> listHistory(UUID applicationId, UUID actorId, String actorRoleClaim);
}
