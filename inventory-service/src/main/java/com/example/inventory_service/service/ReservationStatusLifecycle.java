package com.example.inventory_service.service;

import com.example.inventory_service.entity.InventoryReservation;
import com.example.inventory_service.entity.ReservationStatus;
import com.example.inventory_service.exception.InvalidReservationStatusTransitionException;
import com.example.inventory_service.metrics.InventoryMetrics;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class ReservationStatusLifecycle {

    private final InventoryMetrics inventoryMetrics;

    private final Map<ReservationStatus, Set<ReservationStatus>>
            allowedTransitions = new EnumMap<>(ReservationStatus.class);

    public ReservationStatusLifecycle(InventoryMetrics inventoryMetrics) {

        this.inventoryMetrics = inventoryMetrics;

        allowedTransitions.put(
                ReservationStatus.RESERVED,
                EnumSet.of(
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.RELEASED
                )
        );

        allowedTransitions.put(
                ReservationStatus.CONFIRMED,
                EnumSet.noneOf(ReservationStatus.class)
        );

        allowedTransitions.put(
                ReservationStatus.RELEASED,
                EnumSet.noneOf(ReservationStatus.class)
        );
    }

    public boolean canTransition(ReservationStatus currentStatus,
                                 ReservationStatus targetStatus) {

        if (currentStatus == null || targetStatus == null) {
            return false;
        }

        Set<ReservationStatus> allowed = allowedTransitions.get(currentStatus);

        return allowed != null && allowed.contains(targetStatus);
    }

    public void transition(InventoryReservation reservation,
                           ReservationStatus targetStatus) {

        ReservationStatus currentStatus = reservation.getStatus();

        if (!canTransition(currentStatus,
                targetStatus)) {

            inventoryMetrics.invalidInventoryOperation();

            throw new InvalidReservationStatusTransitionException(
                    currentStatus,
                    targetStatus
            );
        }

        reservation.setStatus(targetStatus);
    }
}