package com.example.fileservice.domain.port.inbound;

import com.example.fileservice.domain.model.File;

public interface GetFileUseCasePort {
    File getFileById(java.util.UUID id);
}
