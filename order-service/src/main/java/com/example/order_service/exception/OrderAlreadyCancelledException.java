package com.example.order_service.exception;

public class OrderAlreadyCancelledException extends RuntimeException {

    public OrderAlreadyCancelledException(Long id) {
        super("Order is already cancelled: " + id);
    }
}