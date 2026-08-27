package com.ecommerce.common.exception;

public class EventIdempotencyException extends RuntimeException {

    public EventIdempotencyException(String message) {
        super(message);
    }

    public EventIdempotencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
