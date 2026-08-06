package com.example.payment_service.exception;


public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long id) {
        super("Payment with ID " + id + " not found");
    }

}
