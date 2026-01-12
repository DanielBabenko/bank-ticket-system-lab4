package com.example.tagservice.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TagEvent {
    private UUID eventId;
    private String eventType;
    private UUID applicationId;
    private UUID actorId;
    private List<String> tagNames;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant timestamp;

    // Конструкторы
    @JsonCreator
    public TagEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("applicationId") UUID applicationId,
            @JsonProperty("actorId") UUID actorId,
            @JsonProperty("tagNames") List<String> tagNames,
            @JsonProperty("timestamp") Instant timestamp) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.applicationId = applicationId;
        this.actorId = actorId;
        this.tagNames = tagNames;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    // Упрощенный конструктор для создания новых событий
    public TagEvent(String eventType, UUID applicationId, UUID actorId, List<String> tagNames) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.applicationId = applicationId;
        this.actorId = actorId;
        this.tagNames = tagNames;
        this.timestamp = Instant.now();
    }

    // Геттеры и сеттеры
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }

    public List<String> getTagNames() { return tagNames; }
    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "TagEvent{" +
                "eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", applicationId=" + applicationId +
                ", actorId=" + actorId +
                ", tagNames=" + tagNames +
                ", timestamp=" + timestamp +
                '}';
    }
}