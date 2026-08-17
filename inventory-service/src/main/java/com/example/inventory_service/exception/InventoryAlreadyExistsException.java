package com.example.inventory_service.exception;

public class InventoryAlreadyExistsException extends RuntimeException {

    public InventoryAlreadyExistsException(Long productId) {
        super("Inventory already exists for product: " + productId);
    }
}