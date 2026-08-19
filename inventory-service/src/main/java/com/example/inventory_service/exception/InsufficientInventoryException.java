package com.example.inventory_service.exception;

import lombok.Getter;

@Getter
public class InsufficientInventoryException extends RuntimeException {

    private final Long productId;

    private final Integer requestedQuantity;

    private final Integer availableQuantity;

    public InsufficientInventoryException(
            Long productId,
            Integer requestedQuantity,
            Integer availableQuantity) {

        super("Insufficient inventory for product "
                        + productId
                        + ". Requested: "
                        + requestedQuantity
                        + ", available: "
                        + availableQuantity
        );

        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
}