package com.example.shipping_service.dto;

public record ShippingProviderResult(
        String carrier,
        String trackingNumber
) {
}