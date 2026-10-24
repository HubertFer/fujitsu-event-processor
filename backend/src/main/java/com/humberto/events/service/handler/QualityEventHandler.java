package com.humberto.events.service.handler;

import com.humberto.events.domain.AuditRecord;
import com.humberto.events.domain.Event;
import com.humberto.events.domain.EventType;
import com.humberto.events.repository.AuditRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QualityEventHandler implements EventTypeHandler {

    private static final Logger log = LoggerFactory.getLogger(QualityEventHandler.class);

    private final AuditRecordRepository auditRecordRepository;

    public QualityEventHandler(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    @Override
    public EventType supports() {
        return EventType.QUALITY;
    }

    @Override
    public void handle(Event event) {
        auditRecordRepository.save(new AuditRecord(event.getId(), "QUALITY_EVENT_REGISTERED"));
        log.info("Audit record created eventId={}", event.getId());
    }
}
