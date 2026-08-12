package com.example.product_service.exception;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("Product with SKU '" + sku + "' already exists");
    }
}