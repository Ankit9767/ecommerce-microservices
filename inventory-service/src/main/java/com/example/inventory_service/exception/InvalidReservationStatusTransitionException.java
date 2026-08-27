package com.example.inventory_service.exception;

import com.example.inventory_service.entity.ReservationStatus;

public class InvalidReservationStatusTransitionException extends RuntimeException {

    public InvalidReservationStatusTransitionException(ReservationStatus current,
                                                       ReservationStatus target) {

        super(
                "Cannot change reservation status from "
                        + current
                        + " to "
                        + target
        );
    }
}
