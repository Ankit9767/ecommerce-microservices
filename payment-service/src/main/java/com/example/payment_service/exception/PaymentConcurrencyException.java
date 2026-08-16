package com.example.payment_service.exception;

public class PaymentConcurrencyException extends RuntimeException {

    public PaymentConcurrencyException(Long orderId) {

        super("Payment creation conflict for order: "
                        + orderId
        );
    }
}