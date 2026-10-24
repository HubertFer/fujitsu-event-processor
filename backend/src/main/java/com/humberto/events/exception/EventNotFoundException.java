package com.humberto.events.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String eventId) {
        super("Event " + eventId + " not found.");
    }
}
