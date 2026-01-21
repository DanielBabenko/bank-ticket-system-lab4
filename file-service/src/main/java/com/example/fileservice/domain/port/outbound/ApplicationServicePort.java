package com.example.fileservice.domain.port.outbound;

import com.example.fileservice.domain.model.ApplicationInfo;
import java.util.List;
import java.util.UUID;

public interface ApplicationServicePort {
    List<ApplicationInfo> getApplicationsByFile(UUID fileId);
}
