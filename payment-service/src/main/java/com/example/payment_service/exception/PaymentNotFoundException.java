package com.example.payment_service.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long id) {
        super("Payment with ID " + id + " not found");
    }

    public PaymentNotFoundException(String providerReference) {
        super("Payment with provider reference "
                + providerReference
                + " not found");
    }
}