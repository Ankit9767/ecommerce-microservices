package com.example.shipping_service.service;

import com.ecommerce.common.events.OrderPaidEvent;
import com.example.shipping_service.entity.Shipment;

public interface ShipmentService {

    Shipment createShipment(OrderPaidEvent event);

    Shipment getShipment(Long shipmentId);

    Shipment getShipmentByOrderId(Long orderId);

    Shipment markShipped(Long shipmentId,
                         String carrier,
                         String trackingNumber);

    Shipment markInTransit(Long shipmentId);

    Shipment markOutForDelivery(Long shipmentId);

    Shipment markDelivered(Long shipmentId);

    Shipment markFailed(Long shipmentId);

    Shipment cancelShipment(Long shipmentId);
}