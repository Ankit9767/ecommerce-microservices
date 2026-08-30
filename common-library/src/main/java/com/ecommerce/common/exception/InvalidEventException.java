package com.ecommerce.common.exception;

public class InvalidEventException extends RuntimeException {

    public InvalidEventException() {
        super("Event must not be null");
    }
}