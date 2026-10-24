package com.humberto.events.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "audit_records")
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false)
    private Instant createdAt;

    protected AuditRecord() {
    }

    public AuditRecord(String eventId, String action) {
        this.eventId = eventId;
        this.action = action;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getAction() { return action; }
    public Instant getCreatedAt() { return createdAt; }
}
