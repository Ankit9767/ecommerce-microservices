package com.ecommerce.common.exception;

public class MissingCustomerIdException extends RuntimeException {

    public MissingCustomerIdException() {
        super("Event must contain customerId");
    }
}
