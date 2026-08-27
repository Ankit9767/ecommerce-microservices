package com.example.inventory_service.repository;

import com.example.inventory_service.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByReservationIdAndProductId(UUID reservationId,
                                                                   Long productId);

    boolean existsByReservationIdAndProductId(UUID reservationId,
                                              Long productId);
}