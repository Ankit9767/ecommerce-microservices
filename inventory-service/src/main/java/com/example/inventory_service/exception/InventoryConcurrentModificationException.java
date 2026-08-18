package com.example.inventory_service.exception;

public class InventoryConcurrentModificationException extends RuntimeException {

    public InventoryConcurrentModificationException(Long productId) {

        super("Inventory was modified concurrently for product: "
                        + productId
        );
    }
}