package com.humberto.events.service;

import com.humberto.events.domain.Event;
import com.humberto.events.domain.EventType;
import com.humberto.events.exception.DuplicateEventException;
import com.humberto.events.exception.EventNotFoundException;
import com.humberto.events.repository.EventRepository;
import com.humberto.events.service.handler.EventTypeHandler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final MeterRegistry meterRegistry;
    private final Map<EventType, EventTypeHandler> handlers = new EnumMap<>(EventType.class);

    public EventService(EventRepository eventRepository, List<EventTypeHandler> handlers, MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.meterRegistry = meterRegistry;
        handlers.forEach(handler -> this.handlers.put(handler.supports(), handler));
        for (EventType type : EventType.values()) {
            if (!this.handlers.containsKey(type)) {
                throw new IllegalStateException("No handler registered for event type " + type);
            }
        }
    }

    @Transactional
    public Event register(Event event) {
        if (eventRepository.existsById(event.getId())) {
            log.warn("Duplicate event rejected eventId={}", event.getId());
            countProcessed(event.getType(), "duplicate");
            throw new DuplicateEventException(event.getId());
        }
        try {
            eventRepository.saveAndFlush(event);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event rejected by database eventId={}", event.getId());
            countProcessed(event.getType(), "duplicate");
            throw new DuplicateEventException(event.getId());
        }
        handlers.get(event.getType()).handle(event);
        log.info("Event registered eventId={} type={}", event.getId(), event.getType());
        countProcessed(event.getType(), "registered");
        return event;
    }

    private void countProcessed(EventType type, String outcome) {
        Counter.builder("events.processed")
                .description("Events received from equipment, by type and outcome")
                .tag("type", type.name())
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();
    }

    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAll(Sort.by(Sort.Direction.DESC, "occurredAt"));
    }

    @Transactional(readOnly = true)
    public Event findById(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }
}