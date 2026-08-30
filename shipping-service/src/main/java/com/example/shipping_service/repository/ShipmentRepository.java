package com.example.shipping_service.repository;

import com.example.shipping_service.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}