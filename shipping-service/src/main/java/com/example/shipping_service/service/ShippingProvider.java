package com.example.shipping_service.service;

import com.example.shipping_service.dto.ShippingProviderResult;

public interface ShippingProvider {

    ShippingProviderResult ship(
            Long shipmentId,
            Long orderId,
            Long customerId
    );
}