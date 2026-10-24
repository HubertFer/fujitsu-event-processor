package com.humberto.events.service.handler;

import com.humberto.events.domain.Event;
import com.humberto.events.domain.EventType;

public interface EventTypeHandler {

    EventType supports();

    void handle(Event event);
}
