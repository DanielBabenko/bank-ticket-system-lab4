package com.example.applicationservice.domain.port.inbound;

import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.util.ApplicationPage;

public interface StreamApplicationsUseCasePort {
    ApplicationPage streamWithNextCursor(String cursor, int limit);
}
