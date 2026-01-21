package com.example.applicationservice.domain.port.inbound;

import com.example.applicationservice.domain.model.entity.Application;
import java.util.UUID;

public interface ChangeStatusUseCasePort {
    Application changeStatus(UUID applicationId, String status, UUID actorId, String actorRoleClaim);
}
