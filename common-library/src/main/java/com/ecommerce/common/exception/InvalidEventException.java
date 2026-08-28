package com.ecommerce.common.exception;

public class InvalidEventException extends RuntimeException {

    public InvalidEventException() {
        super("PaymentCompletedEvent must not be null");
    }
}