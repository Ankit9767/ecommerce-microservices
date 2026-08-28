package com.ecommerce.common.exception;

public class MissingOrderIdException extends RuntimeException {

    public MissingOrderIdException() {
        super("PaymentCompletedEvent must contain orderId");
    }
}
