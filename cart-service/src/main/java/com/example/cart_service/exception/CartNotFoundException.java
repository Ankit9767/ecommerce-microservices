package com.example.cart_service.exception;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(Long customerId) {
        super("Cart not found for customer: " + customerId);
    }
}