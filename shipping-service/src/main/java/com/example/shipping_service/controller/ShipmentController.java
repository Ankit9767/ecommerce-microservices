package com.example.shipping_service.controller;

import com.example.shipping_service.entity.Shipment;
import com.example.shipping_service.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/{shipmentId}")
    @PreAuthorize("hasAuthority('SHIPMENT_READ')")
    public ResponseEntity<Shipment> getShipment(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.getShipment(shipmentId)
        );
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('SHIPMENT_READ')")
    public ResponseEntity<Shipment> getShipmentByOrderId(@PathVariable Long orderId) {

        return ResponseEntity.ok(
                shipmentService.getShipmentByOrderId(orderId)
        );
    }

    @GetMapping("/track/order/{orderId}")
    @PreAuthorize("hasAuthority('SHIPMENT_TRACK')")
    public ResponseEntity<Shipment> trackShipmentByOrderId(@PathVariable Long orderId,
                                                           Authentication authentication) {

        return ResponseEntity.ok(
                shipmentService.trackShipmentByOrderId(
                        orderId,
                        authentication
                )
        );
    }


    @PostMapping("/{shipmentId}/in-transit")
    @PreAuthorize("hasAuthority('SHIPMENT_UPDATE')")
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
    @PreAuthorize("hasAuthority('SHIPMENT_UPDATE')")
    public ResponseEntity<Shipment> markDelivered(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.markDelivered(shipmentId)
        );
    }

    @PostMapping("/{shipmentId}/fail")
    @PreAuthorize("hasAuthority('SHIPMENT_UPDATE')")
    public ResponseEntity<Shipment> markFailed(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.markFailed(shipmentId)
        );
    }

    @PostMapping("/{shipmentId}/cancel")
    @PreAuthorize("hasAuthority('SHIPMENT_CANCEL')")
    public ResponseEntity<Shipment> cancelShipment(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                shipmentService.cancelShipment(shipmentId)
        );
    }
}