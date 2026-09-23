package com.example.product_service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageStorage {

    StoredProductImage store(Long productId, MultipartFile file);

    void delete(String storageKey);

    byte[] load(String storageKey);

    String getContentType(String storageKey);
}