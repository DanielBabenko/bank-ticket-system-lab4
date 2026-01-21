package com.example.fileservice.application.mapper;

import com.example.fileservice.application.dto.ApplicationInfoDto;
import com.example.fileservice.application.dto.FileDto;
import com.example.fileservice.domain.model.ApplicationInfo;
import com.example.fileservice.domain.model.File;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileMapper {

    public static FileDto toDto(File file, List<ApplicationInfo> applications, String downloadUrl) {
        FileDto dto = new FileDto();
        dto.setId(file.getId());
        dto.setOriginalName(file.getOriginalName());
        dto.setMimeType(file.getMimeType());
        dto.setSize(file.getSize());
        dto.setExtension(file.getExtension());
        dto.setUploadDate(file.getUploadDate());
        dto.setUploaderId(file.getUploaderId());
        dto.setDescription(file.getDescription());
        dto.setDownloadUrl(downloadUrl);
        if (applications == null || applications.isEmpty()) {
            dto.setApplications(Collections.emptyList());
        } else {
            dto.setApplications(applications.stream().map(FileMapper::toApplicationInfoDto).collect(Collectors.toList()));
        }
        return dto;
    }

    public static ApplicationInfoDto toApplicationInfoDto(ApplicationInfo ai) {
        ApplicationInfoDto dto = new ApplicationInfoDto();
        dto.setId(ai.getId());
        dto.setApplicantId(ai.getApplicantId());
        dto.setProductId(ai.getProductId());
        dto.setStatus(ai.getStatus());
        dto.setCreatedAt(ai.getCreatedAt());
        return dto;
    }
}
