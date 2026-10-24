package com.humberto.events.domain;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event implements Persistable<String> {

    @Id
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false)
    private Instant receivedAt;

    @Transient
    private boolean isNew = true;

    protected Event() {
    }

    public Event(String eventId, EventType type, String description, LocalDateTime occurredAt) {
        this.eventId = eventId;
        this.type = type;
        this.description = description;
        this.occurredAt = occurredAt;
        this.receivedAt = Instant.now();
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public String getId() {
        return eventId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public EventType getType() { return type; }
    public String getDescription() { return description; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public Instant getReceivedAt() { return receivedAt; }
}
