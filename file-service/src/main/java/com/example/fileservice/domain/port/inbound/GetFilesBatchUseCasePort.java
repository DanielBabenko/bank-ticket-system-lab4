package com.example.fileservice.domain.port.inbound;

import java.util.List;
import java.util.UUID;

public interface GetFilesBatchUseCasePort {
    List<UUID> getFilesBatch(List<UUID> fileIds);
}
