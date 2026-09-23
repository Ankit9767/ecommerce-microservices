package com.example.product_service.storage;

public record StoredProductImage(
        String storageKey,
        String contentType,
        long fileSize
) {
}