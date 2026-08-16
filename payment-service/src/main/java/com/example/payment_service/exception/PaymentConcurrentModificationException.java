package com.example.payment_service.exception;

public class PaymentConcurrentModificationException extends RuntimeException {

    public PaymentConcurrentModificationException(Long paymentId) {

        super("Payment was modified concurrently: "
                        + paymentId
        );
    }
}