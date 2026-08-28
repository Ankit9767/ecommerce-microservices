package com.ecommerce.common.exception;

public class MissingEventIdException extends RuntimeException {

    public MissingEventIdException() {
        super("PaymentCompletedEvent must contain eventId");
    }
}
