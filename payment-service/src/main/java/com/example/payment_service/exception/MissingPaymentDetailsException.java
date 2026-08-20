package com.example.payment_service.exception;

/**
 * Raised when an auto-created payment target (an inbound order event) is
 * missing a field required to create a payment - i.e. currency or payment
 * method. The order flow no longer supplies defaults, so a missing value is
 * a producer bug; the consumer's retry + DLT route handles failure.
 */
public class MissingPaymentDetailsException extends RuntimeException {

    public MissingPaymentDetailsException(Long orderId, String missing) {
        super("Order event " + orderId + " is missing required payment detail: "
                + missing);
    }
}