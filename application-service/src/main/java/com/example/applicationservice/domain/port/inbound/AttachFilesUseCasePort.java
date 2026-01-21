package com.example.applicationservice.domain.port.inbound;

import java.util.List;
import java.util.UUID;

public interface AttachFilesUseCasePort {
    void attachFiles(UUID applicationId, List<UUID> fileIds, UUID actorId, String actorRoleClaim);
}
