package com.example.product_service.service;

import com.example.product_service.entity.Product;

import java.util.List;

public interface ProductService {

    Product createProduct(Product product);

    Product getProduct(Long id);

    List<Product> getAllProducts();

    Product updateProduct(Long id,
                          Product product);

    void deleteProduct(Long id);
}
