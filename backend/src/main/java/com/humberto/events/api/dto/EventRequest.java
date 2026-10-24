package com.humberto.events.api.dto;

import com.humberto.events.domain.Event;
import com.humberto.events.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventRequest(@NotBlank @Size(max = 100) String eventId,
                           @NotNull EventType eventType,
                           @NotBlank @Size(max = 500) String description,
                           @NotNull LocalDateTime timestamp) {

    public Event toEvent() {
        return new Event(eventId.trim(), eventType, description.trim(), timestamp);
    }
}
