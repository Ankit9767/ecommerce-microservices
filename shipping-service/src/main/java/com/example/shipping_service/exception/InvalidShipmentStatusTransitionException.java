package com.example.shipping_service.exception;

public class InvalidShipmentStatusTransitionException
        extends RuntimeException {

    public InvalidShipmentStatusTransitionException(Long shipmentId,
                                                    String currentStatus,
                                                    String requestedStatus) {

        super(
                "Invalid shipment status transition: " +
                        "shipmentId=" + shipmentId +
                        ", currentStatus=" + currentStatus +
                        ", requestedStatus=" + requestedStatus
        );
    }
}