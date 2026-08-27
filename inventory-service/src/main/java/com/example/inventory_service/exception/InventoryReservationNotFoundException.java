package com.example.inventory_service.exception;

import java.util.UUID;

public class InventoryReservationNotFoundException extends RuntimeException {

    public InventoryReservationNotFoundException(UUID reservationId,
                                                 Long productId) {

        super(
                "Inventory reservation not found. " +
                        "reservationId=" + reservationId +
                        ", productId=" + productId
        );
    }
}