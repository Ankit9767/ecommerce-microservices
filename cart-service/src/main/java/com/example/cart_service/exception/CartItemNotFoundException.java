package com.example.cart_service.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long productId) {
        super("Product " + productId + " is not present in the cart");
    }
}