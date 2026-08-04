package com.example.payment_service.Exception;


public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long id) {
        super("Payment with ID " + id + " not found");
    }

}
