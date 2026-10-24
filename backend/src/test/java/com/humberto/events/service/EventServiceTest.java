package com.humberto.events.service;

import com.humberto.events.domain.AuditRecord;
import com.humberto.events.domain.Event;
import com.humberto.events.domain.EventType;
import com.humberto.events.exception.DuplicateEventException;
import com.humberto.events.repository.AuditRecordRepository;
import com.humberto.events.repository.EventRepository;
import com.humberto.events.service.handler.ProductionEventHandler;
import com.humberto.events.service.handler.QualityEventHandler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AuditRecordRepository auditRecordRepository;

    private MeterRegistry meterRegistry;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        eventService = new EventService(eventRepository,
                List.of(new ProductionEventHandler(), new QualityEventHandler(auditRecordRepository)),
                meterRegistry);
    }

    @Test
    void registersProductionEventWithoutAudit() {
        Event event = event("EVT-1", EventType.PRODUCTION);

        eventService.register(event);

        verify(eventRepository).saveAndFlush(event);
        verifyNoInteractions(auditRecordRepository);
        assertThat(processedCount("PRODUCTION", "registered")).isEqualTo(1.0);
    }

    @Test
    void registersQualityEventAndCreatesAudit() {
        Event event = event("EVT-2", EventType.QUALITY);

        eventService.register(event);

        ArgumentCaptor<AuditRecord> audit = ArgumentCaptor.forClass(AuditRecord.class);
        verify(eventRepository).saveAndFlush(event);
        verify(auditRecordRepository).save(audit.capture());
        assertThat(audit.getValue().getEventId()).isEqualTo("EVT-2");
        assertThat(audit.getValue().getAction()).isEqualTo("QUALITY_EVENT_REGISTERED");
    }

    @Test
    void rejectsEventAlreadyProcessed() {
        Event event = event("EVT-3", EventType.QUALITY);
        when(eventRepository.existsById("EVT-3")).thenReturn(true);

        assertThatThrownBy(() -> eventService.register(event))
                .isInstanceOf(DuplicateEventException.class);

        verify(eventRepository, never()).saveAndFlush(any());
        verifyNoInteractions(auditRecordRepository);
        assertThat(processedCount("QUALITY", "duplicate")).isEqualTo(1.0);
    }

    @Test
    void rejectsDuplicateDetectedByDatabase() {
        Event event = event("EVT-4", EventType.QUALITY);
        when(eventRepository.saveAndFlush(event)).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> eventService.register(event))
                .isInstanceOf(DuplicateEventException.class);

        verifyNoInteractions(auditRecordRepository);
    }

    @Test
    void failsWhenEventTypeHasNoHandler() {
        assertThatThrownBy(() -> new EventService(eventRepository, List.of(new ProductionEventHandler()), meterRegistry))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("QUALITY");
    }

    private Event event(String eventId, EventType type) {
        return new Event(eventId, type, "Test event", LocalDateTime.of(2026, 5, 20, 10, 0));
    }

    private double processedCount(String type, String outcome) {
        return meterRegistry.get("events.processed")
                .tag("type", type)
                .tag("outcome", outcome)
                .counter()
                .count();
    }
}
