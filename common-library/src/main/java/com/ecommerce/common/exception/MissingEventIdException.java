package com.ecommerce.common.exception;

public class MissingEventIdException extends RuntimeException {

    public MissingEventIdException() {
        super("Event must contain eventId");
    }
}
