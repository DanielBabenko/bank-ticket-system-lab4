package com.example.applicationservice.domain.port.inbound;

import java.util.List;
import java.util.UUID;

public interface RemoveTagsUseCasePort {
    void removeTags(UUID applicationId, List<String> tagNames, UUID actorId, String actorRoleClaim);
}
