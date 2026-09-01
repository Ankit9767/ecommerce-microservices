package com.example.shipping_service.service.impl;

import com.example.shipping_service.entity.Shipment;
import com.example.shipping_service.enums.ShipmentStatus;
import com.example.shipping_service.repository.ShipmentRepository;
import com.example.shipping_service.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentTrackingSimulator {

    private final ShipmentRepository shipmentRepository;

    private final ShipmentService shipmentService;

    /**
     * Simulates external carrier tracking updates.
     *
     * One scheduler execution advances ONE shipment by ONE state only.
     *
     * SHIPPED
     *      ↓
     * IN_TRANSIT
     *      ↓
     * OUT_FOR_DELIVERY
     *      ↓
     * DELIVERED
     */
    public void processPendingUpdates() {

        if (processShippedShipment()) {
            return;
        }

        if (processInTransitShipment()) {
            return;
        }

        processDeliveryShipment();
    }

    private boolean processShippedShipment() {

        List<Shipment> shipments =
                shipmentRepository.findByStatus(ShipmentStatus.SHIPPED);

        if (shipments.isEmpty()) {
            return false;
        }

        Shipment shipment = shipments.getFirst();

        try {

            shipmentService.markInTransit(shipment.getId());

            log.info(
                    "Mock carrier update: " +
                            "shipmentId={} SHIPPED -> IN_TRANSIT",
                    shipment.getId()
            );

            return true;

        } catch (Exception ex) {

            log.error(
                    "Failed to move shipmentId={} " +
                            "from SHIPPED to IN_TRANSIT",
                    shipment.getId(),
                    ex
            );

            return false;
        }
    }

    private boolean processInTransitShipment() {

        List<Shipment> shipments =
                shipmentRepository.findByStatus(ShipmentStatus.IN_TRANSIT);

        if (shipments.isEmpty()) {
            return false;
        }

        Shipment shipment = shipments.getFirst();

        try {

            shipmentService.markOutForDelivery(shipment.getId());

            log.info(
                    "Mock carrier update: " +
                            "shipmentId={} IN_TRANSIT -> OUT_FOR_DELIVERY",
                    shipment.getId()
            );

            return true;

        } catch (Exception ex) {

            log.error(
                    "Failed to move shipmentId={} " +
                            "from IN_TRANSIT to OUT_FOR_DELIVERY",
                    shipment.getId(),
                    ex
            );

            return false;
        }
    }

    private void processDeliveryShipment() {

        List<Shipment> shipments =
                shipmentRepository.findByStatus(ShipmentStatus.OUT_FOR_DELIVERY);

        if (shipments.isEmpty()) {
            return;
        }

        Shipment shipment = shipments.getFirst();

        try {

            shipmentService.markDelivered(shipment.getId());

            log.info(
                    "Mock carrier update: " +
                            "shipmentId={} OUT_FOR_DELIVERY -> DELIVERED",
                    shipment.getId()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to move shipmentId={} " +
                            "from OUT_FOR_DELIVERY to DELIVERED",
                    shipment.getId(),
                    ex
            );

        }
    }
}