package com.ecommerce.common.exception;

import java.util.UUID;

public class EventAlreadyProcessedException extends RuntimeException {

    private final UUID eventId;

    public EventAlreadyProcessedException(UUID eventId) {
        super("Event has already been processed: " + eventId);
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
}