package com.example.tagservice.application.mapper;

import com.example.tagservice.application.dto.ApplicationInfoDto;
import com.example.tagservice.application.dto.TagDto;
import com.example.tagservice.domain.model.ApplicationInfo;
import com.example.tagservice.domain.model.Tag;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Простой маппер между доменной моделью и DTO слоя приложения.
 * Вынесен в application, чтобы поддерживать единое место преобразований.
 */
public class TagMapper {

    public static TagDto toDto(Tag tag, List<ApplicationInfo> applications) {
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());

        if (applications == null || applications.isEmpty()) {
            dto.setApplications(Collections.emptyList());
        } else {
            dto.setApplications(applications.stream().map(TagMapper::toApplicationInfoDto).collect(Collectors.toList()));
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

    public static TagDto toDtoWithoutApplications(Tag tag) {
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setApplications(Collections.emptyList());
        return dto;
    }
}
