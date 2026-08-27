package com.example.inventory_service.exception;

public class InvalidReservationRequestException extends RuntimeException {

    public InvalidReservationRequestException(String message) {
        super(message);
    }

    public static InvalidReservationRequestException
    missingReservationId(String operation) {

        return new InvalidReservationRequestException(
                "reservationId is required for inventory "
                        + operation
        );
    }

    public static InvalidReservationRequestException
    invalidQuantity(Integer quantity) {

        return new InvalidReservationRequestException(
                "Inventory reservation quantity must be greater than zero. "
                        + "quantity=" + quantity
        );
    }

    public static InvalidReservationRequestException
    missingQuantity() {

        return new InvalidReservationRequestException(
                "Inventory quantity must not be null"
        );
    }
}