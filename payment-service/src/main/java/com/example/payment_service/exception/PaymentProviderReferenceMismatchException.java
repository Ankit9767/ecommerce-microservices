package com.example.payment_service.exception;

public class PaymentProviderReferenceMismatchException
        extends RuntimeException {

    public PaymentProviderReferenceMismatchException(
            Long paymentId,
            String existingReference,
            String newReference) {

        super(
                "Provider reference mismatch for paymentId="
                        + paymentId
                        + ", existingReference="
                        + existingReference
                        + ", newReference="
                        + newReference
        );
    }
}