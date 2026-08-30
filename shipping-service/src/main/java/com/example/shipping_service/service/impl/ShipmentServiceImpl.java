package com.example.shipping_service.service.impl;

import com.ecommerce.common.events.OrderPaidEvent;
import com.example.shipping_service.entity.Shipment;
import com.example.shipping_service.enums.ShipmentStatus;
import com.example.shipping_service.exception.InvalidShipmentStatusTransitionException;
import com.example.shipping_service.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentPersistenceService persistenceService;

    @Override
    @Transactional
    public Shipment createShipment(OrderPaidEvent event) {

        log.info(
                "Creating shipment for orderId={}, customerId={}",
                event.getOrderId(),
                event.getCustomerId()
        );

        return persistenceService.createIfAbsent(
                event.getOrderId(),
                event.getCustomerId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment getShipment(Long shipmentId) {

        return persistenceService.find(shipmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Shipment getShipmentByOrderId(Long orderId) {

        return persistenceService.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Shipment markShipped(Long shipmentId) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.SHIPPED
        );

        shipment.setStatus(ShipmentStatus.SHIPPED);

        shipment.setShippedAt(Instant.now());

        return persistenceService.save(shipment);
    }

    @Transactional
    @Override
    public Shipment markShipped(Long shipmentId,
                                String carrier,
                                String trackingNumber) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.SHIPPED
        );

        shipment.setCarrier(carrier);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setShippedAt(Instant.now());

        log.info(
                "Shipment marked as SHIPPED: shipmentId={}, " +
                        "orderId={}, trackingNumber={}",
                shipmentId,
                shipment.getOrderId(),
                trackingNumber
        );

        return persistenceService.save(shipment);
    }

    @Override
    @Transactional
    public Shipment markInTransit(Long shipmentId) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.IN_TRANSIT
        );

        shipment.setStatus(ShipmentStatus.IN_TRANSIT);

        return persistenceService.save(shipment);
    }

    @Override
    @Transactional
    public Shipment markOutForDelivery(Long shipmentId) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.OUT_FOR_DELIVERY
        );

        shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);

        return persistenceService.save(shipment);
    }

    @Override
    @Transactional
    public Shipment markDelivered(Long shipmentId) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.DELIVERED
        );

        shipment.setStatus(ShipmentStatus.DELIVERED);

        shipment.setDeliveredAt(Instant.now());

        return persistenceService.save(shipment);
    }

    @Transactional
    @Override
    public Shipment markFailed(Long shipmentId) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.FAILED
        );

        shipment.setStatus(ShipmentStatus.FAILED);

        return persistenceService.save(shipment);
    }

    @Override
    @Transactional
    public Shipment cancelShipment(Long shipmentId) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.CANCELLED
        );

        shipment.setStatus(ShipmentStatus.CANCELLED);

        return persistenceService.save(shipment);
    }

    private void validateTransition(
            Shipment shipment,
            ShipmentStatus requestedStatus) {

        ShipmentStatus currentStatus =
                shipment.getStatus();

        boolean valid = switch (currentStatus) {

            case CREATED ->
                    requestedStatus == ShipmentStatus.SHIPPED
                            || requestedStatus == ShipmentStatus.CANCELLED;

            case SHIPPED ->
                    requestedStatus == ShipmentStatus.IN_TRANSIT
                            || requestedStatus == ShipmentStatus.FAILED;

            case IN_TRANSIT ->
                    requestedStatus == ShipmentStatus.OUT_FOR_DELIVERY
                            || requestedStatus == ShipmentStatus.FAILED;

            case OUT_FOR_DELIVERY ->
                    requestedStatus == ShipmentStatus.DELIVERED
                            || requestedStatus == ShipmentStatus.FAILED;

            case DELIVERED,
                 FAILED,
                 CANCELLED -> false;
        };

        if (!valid) {

            throw new InvalidShipmentStatusTransitionException(
                    shipment.getId(),
                    currentStatus.name(),
                    requestedStatus.name()
            );
        }
    }

    @Override
    @Transactional
    public Shipment assignTracking(Long shipmentId,
                                   String carrier,
                                   String trackingNumber) {

        Shipment shipment = persistenceService.find(shipmentId);

        if (shipment.getStatus() == ShipmentStatus.DELIVERED ||
                shipment.getStatus() == ShipmentStatus.CANCELLED) {

            throw new InvalidShipmentStatusTransitionException(
                    shipmentId,
                    shipment.getStatus().name(),
                    "ASSIGN_TRACKING"
            );
        }

        shipment.setCarrier(carrier);

        shipment.setTrackingNumber(trackingNumber);

        return persistenceService.save(shipment);
    }
}