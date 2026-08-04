package com.example.order_service.Exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Order not found with ID : " + id);
    }
}