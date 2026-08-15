package com.example.payment_service.exception;

public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(Long orderId) {
        super("A payment already exists for order: " + orderId);
    }
}