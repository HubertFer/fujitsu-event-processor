package com.humberto.events.exception;

public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException(String eventId) {
        super("Event " + eventId + " has already been processed.");
    }
}
