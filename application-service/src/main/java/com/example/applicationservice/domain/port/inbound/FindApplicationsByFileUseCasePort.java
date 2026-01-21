package com.example.applicationservice.domain.port.inbound;

import com.example.applicationservice.domain.dto.ApplicationInfo;

import java.util.List;
import java.util.UUID;

public interface FindApplicationsByFileUseCasePort {
    List<ApplicationInfo> findApplicationsByFile(UUID fileId);
}
