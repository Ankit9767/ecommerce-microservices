package com.example.shipping_service.exception;

public class InvalidShipmentStatusTransitionException extends RuntimeException {

    public InvalidShipmentStatusTransitionException(
            String message) {

        super(message);
    }
}