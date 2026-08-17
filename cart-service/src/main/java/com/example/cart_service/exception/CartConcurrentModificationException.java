package com.example.cart_service.exception;

public class CartConcurrentModificationException extends RuntimeException {

    public CartConcurrentModificationException(Long customerId) {

        super("Cart was modified by another request. " +
                        "Please retry the operation."
        );
    }
}