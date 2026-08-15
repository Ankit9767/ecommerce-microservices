package com.example.payment_service.exception;

public class PaymentProviderMismatchException extends RuntimeException {

    public PaymentProviderMismatchException(String expected, String actual) {

        super("Payment provider mismatch. Expected: "
                        + expected
                        + ", received: "
                        + actual
        );
    }
}