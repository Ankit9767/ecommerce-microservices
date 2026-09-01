package com.example.shipping_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingTrackingScheduler {

    private final ShipmentTrackingSimulator trackingSimulator;

    @Scheduled(fixedDelay = 30000)
    public void processTrackingUpdates() {

        trackingSimulator.processPendingUpdates();
    }
}