package com.example.shipping_service.exception;

public class ShipmentAlreadyExistsException extends RuntimeException {

    public ShipmentAlreadyExistsException(Long orderId) {

        super(
                "Shipment already exists for order: " +
                        orderId
        );
    }
}