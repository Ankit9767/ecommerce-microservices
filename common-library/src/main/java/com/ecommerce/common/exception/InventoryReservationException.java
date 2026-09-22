package com.ecommerce.common.exception;

public class InventoryReservationException
        extends RuntimeException {

    public InventoryReservationException(String message) {
        super(message);
    }

    public InventoryReservationException(String message,
                                         Throwable cause) {

        super(message, cause);
    }
}