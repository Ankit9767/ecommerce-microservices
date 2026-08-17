package com.example.cart_service.exception;

public class ProductNotAvailableException extends RuntimeException {

    public ProductNotAvailableException(Long productId) {
        super("Product is not available: " + productId);
    }
}