package com.example.fileservice.domain.port.inbound;

import java.util.UUID;

public interface DeleteFileUseCasePort {
    void deleteFile(UUID fileId, UUID userId, Object jwt);
}
