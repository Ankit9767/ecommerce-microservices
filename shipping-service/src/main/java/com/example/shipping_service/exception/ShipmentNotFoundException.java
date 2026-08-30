package com.example.shipping_service.exception;

public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(Long shipmentId) {

        super(
                "Shipment not found: " +
                        shipmentId
        );
    }
}