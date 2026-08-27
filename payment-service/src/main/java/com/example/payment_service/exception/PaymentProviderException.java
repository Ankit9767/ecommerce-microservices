package com.example.payment_service.exception;

public class PaymentProviderException extends RuntimeException {

    public PaymentProviderException(String message) {
        super(message);
    }

    public PaymentProviderException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}