package com.example.payment_service.exception;

public class PaymentAlreadyCompletedException extends RuntimeException {

    public PaymentAlreadyCompletedException(Long paymentId) {
        super("Payment is already completed: " + paymentId);
    }
}