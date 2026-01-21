package com.example.applicationservice.domain.port.inbound;

import com.example.applicationservice.domain.dto.ApplicationInfo;

import java.util.List;

public interface FindApplicationsByTagUseCasePort {
    List<ApplicationInfo> findApplicationsByTag(String tagName);
}
