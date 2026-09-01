package com.ecommerce.common.exception;

public class MissingShipmentIdException extends RuntimeException {

    public MissingShipmentIdException() {
        super("ShipmentEvent must contain shipmentId");
    }
}

