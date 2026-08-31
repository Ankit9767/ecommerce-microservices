package com.example.shipping_service.service;

import com.ecommerce.common.events.*;
import com.ecommerce.common.kafka.EventType;
import com.example.shipping_service.entity.Shipment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentEventFactory {

    public ShipmentShippedEvent buildShipmentShippedEvent(Shipment shipment) {
        return (ShipmentShippedEvent) buildShipmentEvent(
                shipment,
                EventType.SHIPMENT_SHIPPED
        );
    }

    public ShipmentInTransitEvent buildShipmentInTransitEvent(Shipment shipment) {
        return (ShipmentInTransitEvent) buildShipmentEvent(
                shipment,
                EventType.SHIPMENT_IN_TRANSIT
        );
    }

    public ShipmentOutForDeliveryEvent buildShipmentOutForDeliveryEvent(Shipment shipment) {
        return (ShipmentOutForDeliveryEvent) buildShipmentEvent(
                shipment,
                EventType.SHIPMENT_OUT_FOR_DELIVERY
        );
    }

    public ShipmentDeliveredEvent buildShipmentDeliveredEvent(Shipment shipment) {
        return (ShipmentDeliveredEvent) buildShipmentEvent(
                shipment,
                EventType.SHIPMENT_DELIVERED
        );
    }

    public ShipmentFailedEvent buildShipmentFailedEvent(Shipment shipment) {
        return (ShipmentFailedEvent) buildShipmentEvent(
                shipment,
                EventType.SHIPMENT_FAILED
        );
    }

    public ShipmentCancelledEvent buildShipmentCancelledEvent(Shipment shipment) {
        return (ShipmentCancelledEvent) buildShipmentEvent(
                shipment,
                EventType.SHIPMENT_CANCELLED
        );
    }


    public ShipmentEvent buildShipmentEvent(Shipment shipment,
                                            EventType eventType) {

        return switch (eventType) {

            case SHIPMENT_SHIPPED ->
                    ShipmentShippedEvent.builder()
                            .eventType(eventType)
                            .shipmentId(shipment.getId())
                            .orderId(shipment.getOrderId())
                            .customerId(shipment.getCustomerId())
                            .carrier(shipment.getCarrier())
                            .trackingNumber(shipment.getTrackingNumber())
                            .build();

            case SHIPMENT_IN_TRANSIT ->
                    ShipmentInTransitEvent.builder()
                            .eventType(eventType)
                            .shipmentId(shipment.getId())
                            .orderId(shipment.getOrderId())
                            .customerId(shipment.getCustomerId())
                            .carrier(shipment.getCarrier())
                            .trackingNumber(shipment.getTrackingNumber())
                            .build();

            case SHIPMENT_OUT_FOR_DELIVERY ->
                    ShipmentOutForDeliveryEvent.builder()
                            .eventType(eventType)
                            .shipmentId(shipment.getId())
                            .orderId(shipment.getOrderId())
                            .customerId(shipment.getCustomerId())
                            .carrier(shipment.getCarrier())
                            .trackingNumber(shipment.getTrackingNumber())
                            .build();

            case SHIPMENT_DELIVERED ->
                    ShipmentDeliveredEvent.builder()
                            .eventType(eventType)
                            .shipmentId(shipment.getId())
                            .orderId(shipment.getOrderId())
                            .customerId(shipment.getCustomerId())
                            .carrier(shipment.getCarrier())
                            .trackingNumber(shipment.getTrackingNumber())
                            .build();

            case SHIPMENT_FAILED ->
                    ShipmentFailedEvent.builder()
                            .eventType(eventType)
                            .shipmentId(shipment.getId())
                            .orderId(shipment.getOrderId())
                            .customerId(shipment.getCustomerId())
                            .carrier(shipment.getCarrier())
                            .trackingNumber(shipment.getTrackingNumber())
                            .build();

            case SHIPMENT_CANCELLED ->
                    ShipmentCancelledEvent.builder()
                            .eventType(eventType)
                            .shipmentId(shipment.getId())
                            .orderId(shipment.getOrderId())
                            .customerId(shipment.getCustomerId())
                            .carrier(shipment.getCarrier())
                            .trackingNumber(shipment.getTrackingNumber())
                            .build();

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported shipment event type: " + eventType
                    );
        };
    }
}


