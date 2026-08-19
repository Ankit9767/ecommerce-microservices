package com.example.inventory_service.exception;

public class InvalidInventoryQuantityException extends RuntimeException {

    public InvalidInventoryQuantityException() {

        super(
                "Inventory quantity must be greater than zero"
        );
    }
}