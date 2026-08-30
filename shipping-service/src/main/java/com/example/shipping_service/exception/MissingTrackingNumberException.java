package com.example.shipping_service.exception;

public class MissingTrackingNumberException extends RuntimeException {

    public MissingTrackingNumberException() {
        super("Tracking number is required when shipping a shipment");
    }
}
