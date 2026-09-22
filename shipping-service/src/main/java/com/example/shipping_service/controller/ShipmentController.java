package com.example.shipping_service.controller;

import com.example.shipping_service.dto.ShipmentResponse;
import com.example.shipping_service.entity.Shipment;
import com.example.shipping_service.mapper.ShipmentMapper;
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

    private final ShipmentMapper shipmentMapper;

    @GetMapping("/{shipmentId}")
    @PreAuthorize("hasAuthority('SHIPMENT_READ')")
    public ResponseEntity<ShipmentResponse> getShipment(
            @PathVariable Long shipmentId) {

        Shipment shipment =
                shipmentService.getShipment(shipmentId);

        return ResponseEntity.ok(
                shipmentMapper.toResponse(shipment)
        );
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('SHIPMENT_READ')")
    public ResponseEntity<ShipmentResponse> getShipmentByOrderId(
            @PathVariable Long orderId) {

        Shipment shipment =
                shipmentService.getShipmentByOrderId(orderId);

        return ResponseEntity.ok(
                shipmentMapper.toResponse(shipment)
        );
    }

    @GetMapping("/track/order/{orderId}")
    @PreAuthorize("hasAuthority('SHIPMENT_TRACK')")
    public ResponseEntity<ShipmentResponse> trackShipmentByOrderId(
            @PathVariable Long orderId,
            Authentication authentication) {

        Shipment shipment =
                shipmentService.trackShipmentByOrderId(
                        orderId,
                        authentication
                );

        return ResponseEntity.ok(
                shipmentMapper.toResponse(shipment)
        );
    }

    @PostMapping("/{shipmentId}/in-transit")
    @PreAuthorize("hasAuthority('SHIPMENT_UPDATE')")
    public ResponseEntity<ShipmentResponse> markInTransit(
            @PathVariable Long shipmentId) {

        Shipment shipment =
                shipmentService.markInTransit(shipmentId);

        return ResponseEntity.ok(
                shipmentMapper.toResponse(shipment)
        );
    }

    @PostMapping("/{shipmentId}/out-for-delivery")
    public ResponseEntity<ShipmentResponse> markOutForDelivery(
            @PathVariable Long shipmentId) {

        Shipment shipment =
                shipmentService.markOutForDelivery(shipmentId);

        return ResponseEntity.ok(
                shipmentMapper.toResponse(shipment)
        );
    }

    @PostMapping("/{shipmentId}/deliver")
    @PreAuthorize("hasAuthority('SHIPMENT_UPDATE')")
    public ResponseEntity<ShipmentResponse> markDelivered(
            @PathVariable Long shipmentId) {

        Shipment shipment =
                shipmentService.markDelivered(shipmentId);

        return ResponseEntity.ok(
                shipmentMapper.toResponse(shipment)
        );
    }

    @PostMapping("/{shipmentId}/fail")
    @PreAuthorize("hasAuthority('SHIPMENT_UPDATE')")
    public ResponseEntity<ShipmentResponse> markFailed(
            @PathVariable Long shipmentId) {

        Shipment shipment =
                shipmentService.markFailed(shipmentId);

        return ResponseEntity.ok(
                shipmentMapper.toResponse(shipment)
        );
    }

    @PostMapping("/{shipmentId}/cancel")
    @PreAuthorize("hasAuthority('SHIPMENT_CANCEL')")
    public ResponseEntity<ShipmentResponse> cancelShipment(
            @PathVariable Long shipmentId) {

        Shipment shipment =
                shipmentService.cancelShipment(shipmentId);

        return ResponseEntity.ok(
                shipmentMapper.toResponse(shipment)
        );
    }
}