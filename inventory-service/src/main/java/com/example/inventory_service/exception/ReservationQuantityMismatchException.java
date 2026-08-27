package com.example.inventory_service.exception;

import java.util.UUID;

public class ReservationQuantityMismatchException extends RuntimeException {

    public ReservationQuantityMismatchException(
            UUID reservationId,
            Long productId,
            Integer existingQuantity,
            Integer requestedQuantity) {

        super(
                "Reservation quantity mismatch. " +
                        "reservationId=" + reservationId +
                        ", productId=" + productId +
                        ", existingQuantity=" + existingQuantity +
                        ", requestedQuantity=" + requestedQuantity
        );
    }
}