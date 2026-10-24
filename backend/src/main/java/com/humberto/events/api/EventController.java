package com.humberto.events.api;

import com.humberto.events.api.dto.EventRequest;
import com.humberto.events.api.dto.EventResponse;
import com.humberto.events.domain.Event;
import com.humberto.events.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> register(@Valid @RequestBody EventRequest request) {
        Event event = eventService.register(request.toEvent());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{eventId}")
                .buildAndExpand(event.getId())
                .toUri();
        return ResponseEntity.created(location).body(EventResponse.from(event));
    }

    @GetMapping
    public List<EventResponse> findAll() {
        return eventService.findAll().stream()
                .map(EventResponse::from)
                .toList();
    }

    @GetMapping("/{eventId}")
    public EventResponse findById(@PathVariable String eventId) {
        return EventResponse.from(eventService.findById(eventId));
    }
}


