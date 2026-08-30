package com.example.shipping_service.exception;

public class TrackingInformationAlreadyAssignedException
        extends RuntimeException {

    public TrackingInformationAlreadyAssignedException() {
        super("Tracking information is already assigned");
    }
}
