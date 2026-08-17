package com.example.inventory_service.exception;

public class InvalidInventoryOperationException extends RuntimeException {

    public InvalidInventoryOperationException(String message) {
        super(message);
    }
}