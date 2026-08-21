package com.ecommerce.common.exception;

public class OutboxEventCreationException extends RuntimeException {

    public OutboxEventCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
