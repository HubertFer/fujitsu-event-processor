package com.humberto.events.api.dto;

import com.humberto.events.domain.Event;

import java.time.Instant;
import java.time.LocalDateTime;

public record EventResponse(
        String eventId,
        String eventType,
        String description,
        LocalDateTime timestamp,
        Instant receivedAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getType().name(),
                event.getDescription(),
                event.getOccurredAt(),
                event.getReceivedAt()
        );
    }
}
