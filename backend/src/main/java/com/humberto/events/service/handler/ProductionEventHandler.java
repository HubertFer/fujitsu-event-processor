package com.humberto.events.service.handler;

import com.humberto.events.domain.Event;
import com.humberto.events.domain.EventType;
import org.springframework.stereotype.Component;

@Component
public class ProductionEventHandler implements EventTypeHandler {

    @Override
    public EventType supports() {
        return EventType.PRODUCTION;
    }

    @Override
    public void handle(Event event) {
    }
}
