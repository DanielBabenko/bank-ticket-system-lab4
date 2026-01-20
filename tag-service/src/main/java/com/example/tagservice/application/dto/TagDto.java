package com.example.tagservice.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO, которым оперирует слой адаптеров (API) — представление тега вместе
 * со списком связанных applications.
 */
public class TagDto {
    private UUID id;
    private String name;
    private List<ApplicationInfoDto> applications;

    public TagDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ApplicationInfoDto> getApplications() { return applications; }
    public void setApplications(List<ApplicationInfoDto> applications) { this.applications = applications; }
}
