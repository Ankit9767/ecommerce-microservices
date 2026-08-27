package com.example.inventory_service.exception;

import com.example.inventory_service.entity.ReservationStatus;

import java.util.UUID;

public class InvalidReservationStateException extends RuntimeException {

    public InvalidReservationStateException(
            UUID reservationId,
            Long productId,
            ReservationStatus currentStatus,
            String operation) {

        super(
                "Cannot " + operation +
                        " inventory reservation. " +
                        "reservationId=" + reservationId +
                        ", productId=" + productId +
                        ", currentStatus=" + currentStatus
        );
    }
}