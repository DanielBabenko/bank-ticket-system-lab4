package com.example.applicationservice.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class UserDeletedEvent {
    private final UUID userId;
    private final Instant timestamp;

    @JsonCreator
    public UserDeletedEvent(@JsonProperty("userId") UUID userId,
                            @JsonProperty("timestamp") Instant timestamp) {
        this.userId = userId;
        // Если в сообщении нет timestamp, используем текущее время.
        // Это важно для корректной десериализации.
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    // Геттеры обязательны для Jackson
    public UUID getUserId() { return userId; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "UserDeletedEvent{userId=" + userId + ", timestamp=" + timestamp + '}';
    }
}