package com.example.applicationservice.application.mapper;

import com.example.applicationservice.application.dto.*;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;

import java.util.ArrayList;

/**
 * Простой маппер domain <-> application DTO
 */
public class ApplicationMapper {

    public static ApplicationDto toDto(Application app) {
        if (app == null) return null;
        ApplicationDto dto = new ApplicationDto();
        dto.setId(app.getId());
        dto.setApplicantId(app.getApplicantId());
        dto.setProductId(app.getProductId());
        dto.setStatus(app.getStatus());
        dto.setCreatedAt(app.getCreatedAt());

        if (app.getFiles() != null) {
            dto.setFiles(new ArrayList<>(app.getFiles()));
        }
        if (app.getTags() != null) {
            dto.setTags(new ArrayList<>(app.getTags()));
        }
        return dto;
    }

    public static ApplicationInfoDto toInfoDto(Application app) {
        if (app == null) return null;
        ApplicationInfoDto dto = new ApplicationInfoDto();
        dto.setId(app.getId());
        dto.setApplicantId(app.getApplicantId());
        dto.setProductId(app.getProductId());
        dto.setStatus(app.getStatus() != null ? app.getStatus().name() : null);
        dto.setCreatedAt(app.getCreatedAt());
        return dto;
    }

    public static ApplicationHistoryDto toHistoryDto(ApplicationHistory history) {
        if (history == null) return null;
        ApplicationHistoryDto dto = new ApplicationHistoryDto();
        dto.setId(history.getId());
        dto.setApplicationId(history.getApplicationId());
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setChangedByRole(history.getChangedBy());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }
}
