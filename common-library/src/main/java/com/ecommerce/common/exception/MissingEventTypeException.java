package com.ecommerce.common.exception;

public class MissingEventTypeException extends RuntimeException {

    public MissingEventTypeException() {
        super("PaymentCompletedEvent must contain eventType");
    }
}
