package com.example.payment_service.exception;

public class InvalidPaymentProviderResponseException
        extends RuntimeException {

    public InvalidPaymentProviderResponseException(
            Long paymentId) {

        super(
                "Invalid payment provider response for paymentId="
                        + paymentId
        );
    }
}