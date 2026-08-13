package com.example.product_service.service;

import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.dto.UpdateProductRequest;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProduct(Long id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(
            Long id,
            UpdateProductRequest request
    );

    void deactivateProduct(Long id);
}