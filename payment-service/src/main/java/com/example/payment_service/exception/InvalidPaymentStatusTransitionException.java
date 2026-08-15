package com.example.payment_service.exception;

import com.ecommerce.common.enums.PaymentStatus;

public class InvalidPaymentStatusTransitionException extends RuntimeException {

    public InvalidPaymentStatusTransitionException(PaymentStatus currentStatus,
                                                   PaymentStatus targetStatus) {

        super("Invalid payment status transition: "
                        + currentStatus
                        + " -> "
                        + targetStatus
        );
    }
}