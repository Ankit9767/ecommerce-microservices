package com.example.shipping_service.dto;

import com.example.shipping_service.entity.Shipment;

public record ShipmentCreationResult(
        Shipment shipment,
        boolean created
) {
}