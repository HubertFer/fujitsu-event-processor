package com.humberto.events.api.dto;

import com.humberto.events.domain.AuditRecord;

import java.time.Instant;

public record AuditRecordResponse(Long id, String eventId, String action, Instant createdAt) {

    public static AuditRecordResponse from(AuditRecord record) {
        return new AuditRecordResponse(record.getId(), record.getEventId(), record.getAction(), record.getCreatedAt());
    }
}
