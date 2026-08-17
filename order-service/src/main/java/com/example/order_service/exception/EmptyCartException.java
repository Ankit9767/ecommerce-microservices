package com.example.order_service.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot create order because the cart is empty");
    }
}