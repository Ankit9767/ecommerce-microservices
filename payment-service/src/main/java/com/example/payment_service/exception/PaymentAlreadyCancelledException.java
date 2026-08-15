package com.example.payment_service.exception;

public class PaymentAlreadyCancelledException extends RuntimeException {

    public PaymentAlreadyCancelledException(Long paymentId) {
        super("Payment is already cancelled: " + paymentId);
    }
}