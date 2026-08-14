package com.example.order_service.exception;

public class DuplicateOrderProductException extends RuntimeException {

    private final Long productId;

    public DuplicateOrderProductException(Long productId) {
        super("Product with id " + productId + " appears more than once in the order");
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
