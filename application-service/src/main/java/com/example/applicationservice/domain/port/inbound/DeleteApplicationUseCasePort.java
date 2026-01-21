package com.example.applicationservice.domain.port.inbound;

import java.util.UUID;

public interface DeleteApplicationUseCasePort {
    void deleteApplication(UUID applicationId, UUID actorId, String actorRoleClaim);
}
