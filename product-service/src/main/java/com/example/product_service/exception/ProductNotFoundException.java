package com.example.product_service.exception;

public class ProductNotFoundException extends RuntimeException{

    public ProductNotFoundException(Long id){
        super("Product not found with ID : " + id);
    }
}
