package com.example.inventory_service.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(Long productId,
                                          Integer requested,
                                          Integer available) {

        super("Insufficient inventory for product "
                        + productId
                        + ". Requested: "
                        + requested
                        + ", available: "
                        + available
        );
    }
}