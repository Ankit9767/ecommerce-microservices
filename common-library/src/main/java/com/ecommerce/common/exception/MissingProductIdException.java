package com.ecommerce.common.exception;

public class MissingProductIdException extends RuntimeException {

    public MissingProductIdException() {
        super("Event must contain productId");
    }
}

