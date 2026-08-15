package com.example.payment_service.exception;

public class PaymentNotPayableException extends RuntimeException {

    public PaymentNotPayableException(Long orderId) {
        super("Order is not currently eligible for payment: " + orderId);
    }
}