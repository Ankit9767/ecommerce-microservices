package com.example.shipping_service.controller;

import com.example.shipping_service.entity.Shipment;
import com.example.shipping_service.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/{shipmentId}")
    public ResponseEntity<Shipment> getShipment(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.getShipment(shipmentId)
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getShipmentByOrderId(@PathVariable Long orderId) {

        return ResponseEntity.ok(
                shipmentService.getShipmentByOrderId(orderId)
        );
    }

    @PostMapping("/{shipmentId}/ship")
    public ResponseEntity<Shipment> markShipped(@PathVariable Long shipmentId,
                                                @RequestParam String carrier,
                                                @RequestParam String trackingNumber) {

        return ResponseEntity.ok(
                shipmentService.markShipped(
                        shipmentId,
                        carrier,
                        trackingNumber
                )
        );
    }

    @PostMapping("/{shipmentId}/in-transit")
    public ResponseEntity<Shipment> markInTransit(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.markInTransit(shipmentId)
        );
    }

    @PostMapping("/{shipmentId}/out-for-delivery")
    public ResponseEntity<Shipment> markOutForDelivery(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.markOutForDelivery(shipmentId)
        );
    }

    @PostMapping("/{shipmentId}/deliver")
    public ResponseEntity<Shipment> markDelivered(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.markDelivered(shipmentId)
        );
    }

    @PostMapping("/{shipmentId}/fail")
    public ResponseEntity<Shipment> markFailed(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.markFailed(shipmentId)
        );
    }

    @PostMapping("/{shipmentId}/cancel")
    public ResponseEntity<Shipment> cancelShipment(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.cancelShipment(shipmentId)
        );
    }
}