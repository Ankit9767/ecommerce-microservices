package com.ecommerce.common.exception;

public class MissingPaymentIdException extends RuntimeException {

    public MissingPaymentIdException() {
        super("PaymentCompletedEvent must contain paymentId");
    }
}
