package com.example.payment_service.exception;

public class PaymentOrderAccessDeniedException extends RuntimeException {

    public PaymentOrderAccessDeniedException(Long orderId) {
        super("You are not authorized to create a payment for order: " + orderId);
    }
}