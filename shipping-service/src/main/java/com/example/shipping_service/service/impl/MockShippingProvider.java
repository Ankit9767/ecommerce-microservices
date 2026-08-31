package com.example.shipping_service.service.impl;

import com.example.shipping_service.dto.ShippingProviderResult;
import com.example.shipping_service.service.ShippingProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile("!test")
public class MockShippingProvider implements ShippingProvider {

    private static final String CARRIER = "MOCK_CARRIER";

    @Override
    public ShippingProviderResult ship(Long shipmentId,
                                       Long orderId,
                                       Long customerId) {

        String trackingNumber =
                "MOCK-" +
                        orderId +
                        "-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase();

        log.info(
                "Mock shipping provider accepted shipment: " +
                        "shipmentId={}, orderId={}, customerId={}, " +
                        "carrier={}, trackingNumber={}",
                shipmentId,
                orderId,
                customerId,
                CARRIER,
                trackingNumber
        );

        return new ShippingProviderResult(
                CARRIER,
                trackingNumber
        );
    }
}