package com.example.shipping_service.exception;

public class MissingCarrierException extends RuntimeException {

    public MissingCarrierException() {
        super("Carrier is required when shipping a shipment");
    }
}
