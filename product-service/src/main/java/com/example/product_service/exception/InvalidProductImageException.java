package com.example.product_service.exception;

import lombok.Getter;

@Getter
public class InvalidProductImageException extends RuntimeException {

    public InvalidProductImageException(String message) {
        super(message);
    }
}