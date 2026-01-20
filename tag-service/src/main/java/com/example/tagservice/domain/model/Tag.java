package com.example.tagservice.domain.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Доменная сущность Tag. Не содержит фреймворк-зависимостей (без JPA-аннотаций).
 * Содержит минимальное поведение: создание экземпляра и базовая валидация.
 */
public class Tag {

    private UUID id;
    private String name;
    private Set<UUID> applicationIds = new HashSet<>();

    public Tag() {}

    private Tag(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Tag createNew(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name required");
        }
        return new Tag(UUID.randomUUID(), name.trim());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name required");
        }
        this.name = name.trim();
    }

    public Set<UUID> getApplicationIds() { return applicationIds; }
    public void setApplicationIds(Set<UUID> applicationIds) {
        this.applicationIds = (applicationIds == null) ? new HashSet<>() : new HashSet<>(applicationIds);
    }

    public void addApplicationId(UUID applicationId) {
        if (applicationId != null) {
            this.applicationIds.add(applicationId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id) &&
                Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
