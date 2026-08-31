package com.example.shipping_service.service.impl;

import com.ecommerce.common.events.OrderPaidEvent;
import com.ecommerce.common.events.ShipmentCancelledEvent;
import com.ecommerce.common.events.ShipmentDeliveredEvent;
import com.ecommerce.common.events.ShipmentFailedEvent;
import com.ecommerce.common.events.ShipmentInTransitEvent;
import com.ecommerce.common.events.ShipmentOutForDeliveryEvent;
import com.ecommerce.common.events.ShipmentShippedEvent;
import com.example.shipping_service.dto.ShipmentCreationResult;
import com.example.shipping_service.dto.ShippingProviderResult;
import com.example.shipping_service.entity.Shipment;
import com.example.shipping_service.enums.ShipmentStatus;
import com.example.shipping_service.exception.InvalidShipmentStatusTransitionException;
import com.example.shipping_service.exception.MissingCarrierException;
import com.example.shipping_service.exception.MissingTrackingNumberException;
import com.example.shipping_service.service.ShipmentEventFactory;
import com.example.shipping_service.service.ShipmentService;
import com.example.shipping_service.service.ShippingOutboxService;
import com.example.shipping_service.service.ShippingProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentPersistenceService persistenceService;

    private final ShippingOutboxService outboxService;

    private final ShippingProvider shippingProvider;

    private final ShipmentEventFactory shipmentEventFactory;


    @Override
    @Transactional
    public Shipment createShipment(OrderPaidEvent event) {

        log.info(
                "Creating shipment for orderId={}, customerId={}",
                event.getOrderId(),
                event.getCustomerId()
        );

        ShipmentCreationResult result =
                persistenceService.createIfAbsent(
                        event.getOrderId(),
                        event.getCustomerId(),
                        event.getRecipientEmail()
                );

        Shipment shipment = result.shipment();

        /*
         * This can happen when the same OrderPaidEvent
         * is delivered more than once.
         *
         * Do not call the shipping provider again.
         * Do not create another outbox event.
         */
        if (!result.created()) {

            return shipment;
        }

        ShippingProviderResult providerResult =
                shippingProvider.ship(
                        shipment.getId(),
                        shipment.getOrderId(),
                        shipment.getCustomerId()
                );

        Shipment shipped =
                markShipped(
                        shipment.getId(),
                        providerResult.carrier(),
                        providerResult.trackingNumber()
                );

        log.info(
                "Shipment created and shipped successfully: " +
                        "shipmentId={}, orderId={}, carrier={}, " +
                        "trackingNumber={}",
                shipped.getId(),
                shipped.getOrderId(),
                providerResult.carrier(),
                providerResult.trackingNumber()
        );

        return shipped;
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
    public Shipment markShipped(Long shipmentId,
                                String carrier,
                                String trackingNumber) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.SHIPPED
        );

        validateTrackingInformation(
                carrier,
                trackingNumber
        );

        shipment.setCarrier(carrier);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setShippedAt(LocalDateTime.now());

        Shipment saved = persistenceService.save(shipment);

        ShipmentShippedEvent shippedEvent =
                shipmentEventFactory.buildShipmentShippedEvent(saved);

        outboxService.saveShipmentEvent(shippedEvent);

        log.info(
                "Shipment marked as SHIPPED: " +
                        "shipmentId={}, orderId={}, " +
                        "carrier={}, trackingNumber={}",
                shipmentId,
                shipment.getOrderId(),
                carrier,
                trackingNumber
        );

        return saved;
    }

    private void validateTrackingInformation(String carrier,
                                             String trackingNumber) {

        if (carrier == null || carrier.isBlank()) {

            throw new MissingCarrierException();
        }

        if (trackingNumber == null ||
                trackingNumber.isBlank()) {

            throw new MissingTrackingNumberException();
        }
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

        Shipment saved = persistenceService.save(shipment);

        ShipmentInTransitEvent inTransitEvent =
                shipmentEventFactory.buildShipmentInTransitEvent(saved);

        outboxService.saveShipmentEvent(inTransitEvent);

        log.info(
                "Shipment marked as IN_TRANSIT: " +
                        "shipmentId={}, orderId={}",
                shipmentId,
                shipment.getOrderId()
        );

        return saved;
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

        Shipment saved = persistenceService.save(shipment);

        ShipmentOutForDeliveryEvent outForDeliveryEvent =
                shipmentEventFactory.buildShipmentOutForDeliveryEvent(saved);

        outboxService.saveShipmentEvent(outForDeliveryEvent);

        log.info(
                "Shipment marked as OUT_FOR_DELIVERY: " +
                        "shipmentId={}, orderId={}",
                shipmentId,
                shipment.getOrderId()
        );

        return saved;
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

        shipment.setDeliveredAt(LocalDateTime.now());

        Shipment saved = persistenceService.save(shipment);

        ShipmentDeliveredEvent deliveredEvent =
                shipmentEventFactory.buildShipmentDeliveredEvent(saved);;

        outboxService.saveShipmentEvent(deliveredEvent);

        log.info(
                "Shipment marked as DELIVERED: " +
                        "shipmentId={}, orderId={}",
                shipmentId,
                shipment.getOrderId()
        );

        return saved;
    }

    @Override
    @Transactional
    public Shipment markFailed(Long shipmentId) {

        Shipment shipment = persistenceService.find(shipmentId);

        validateTransition(
                shipment,
                ShipmentStatus.FAILED
        );

        shipment.setStatus(ShipmentStatus.FAILED);

        Shipment saved = persistenceService.save(shipment);

        ShipmentFailedEvent failedEvent =
                shipmentEventFactory.buildShipmentFailedEvent(saved);

        outboxService.saveShipmentEvent(failedEvent);

        log.info(
                "Shipment marked as FAILED: " +
                        "shipmentId={}, orderId={}",
                shipmentId,
                shipment.getOrderId()
        );

        return saved;
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

        Shipment saved = persistenceService.save(shipment);

        ShipmentCancelledEvent cancelledEvent =
                shipmentEventFactory.buildShipmentCancelledEvent(saved);

        outboxService.saveShipmentEvent(cancelledEvent);

        log.info(
                "Shipment cancelled: " +
                        "shipmentId={}, orderId={}",
                shipmentId,
                shipment.getOrderId()
        );

        return saved;
    }

    private void validateTransition(Shipment shipment,
                                    ShipmentStatus requestedStatus) {

        ShipmentStatus currentStatus = shipment.getStatus();

        boolean valid = switch (currentStatus) {

            case CREATED ->
                    requestedStatus ==
                            ShipmentStatus.SHIPPED
                            ||
                            requestedStatus ==
                                    ShipmentStatus.CANCELLED;

            case SHIPPED ->
                    requestedStatus ==
                            ShipmentStatus.IN_TRANSIT
                            ||
                            requestedStatus ==
                                    ShipmentStatus.FAILED;

            case IN_TRANSIT ->
                    requestedStatus ==
                            ShipmentStatus.OUT_FOR_DELIVERY
                            ||
                            requestedStatus ==
                                    ShipmentStatus.FAILED;

            case OUT_FOR_DELIVERY ->
                    requestedStatus ==
                            ShipmentStatus.DELIVERED
                            ||
                            requestedStatus ==
                                    ShipmentStatus.FAILED;

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
}