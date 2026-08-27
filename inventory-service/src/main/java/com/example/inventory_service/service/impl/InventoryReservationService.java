package com.example.inventory_service.service.impl;

import com.example.inventory_service.entity.InventoryReservation;
import com.example.inventory_service.entity.ReservationStatus;
import com.example.inventory_service.exception.InvalidReservationStateException;
import com.example.inventory_service.exception.ReservationQuantityMismatchException;
import com.example.inventory_service.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final InventoryReservationRepository repository;

    @Transactional(readOnly = true)
    public InventoryReservation find(UUID reservationId,
                                     Long productId) {

        return repository
                .findByReservationIdAndProductId(
                        reservationId,
                        productId
                )
                .orElse(null);
    }

    @Transactional
    public InventoryReservation create(UUID reservationId,
                                       Long productId,
                                       Integer quantity) {

        InventoryReservation existing = find(reservationId, productId);

        if (existing != null) {

            validateQuantity(
                    existing,
                    quantity
            );

            return existing;
        }

        InventoryReservation reservation =
                InventoryReservation.builder()
                        .reservationId(reservationId)
                        .productId(productId)
                        .quantity(quantity)
                        .status(ReservationStatus.RESERVED)
                        .build();

        try {

            return repository.saveAndFlush(reservation);

        } catch (DataIntegrityViolationException ex) {

            /*
             * Another request may have inserted the same
             * reservation concurrently.
             */
            InventoryReservation concurrent =
                    repository
                            .findByReservationIdAndProductId(
                                    reservationId,
                                    productId
                            )
                            .orElseThrow(() -> ex);

            validateQuantity(concurrent, quantity);

            return concurrent;
        }
    }

    @Transactional
    public InventoryReservation getRequired(UUID reservationId,
                                            Long productId) {

        return repository
                .findByReservationIdAndProductId(
                        reservationId,
                        productId
                )
                .orElseThrow(() ->
                        new com.example.inventory_service.exception
                                .InventoryReservationNotFoundException(
                                reservationId,
                                productId
                        )
                );
    }

    @Transactional
    public void release(UUID reservationId, Long productId) {

        InventoryReservation reservation =
                getRequired(
                        reservationId,
                        productId
                );

        if (reservation.getStatus() == ReservationStatus.RELEASED) {

            return;
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {

            throw new InvalidReservationStateException(
                    reservationId,
                    productId,
                    reservation.getStatus(),
                    "release"
            );
        }

        reservation.setStatus(ReservationStatus.RELEASED);

        repository.save(reservation);
    }

    @Transactional
    public void confirm(UUID reservationId, Long productId) {

        InventoryReservation reservation =
                getRequired(
                        reservationId,
                        productId
                );

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {

            return;
        }

        if (reservation.getStatus() == ReservationStatus.RELEASED) {

            throw new InvalidReservationStateException(
                    reservationId,
                    productId,
                    reservation.getStatus(),
                    "confirm"
            );
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);

        repository.save(reservation);
    }

    private void validateQuantity(InventoryReservation reservation,
                                  Integer requestedQuantity) {

        if (!reservation.getQuantity()
                .equals(requestedQuantity)) {

            throw new ReservationQuantityMismatchException(
                    reservation.getReservationId(),
                    reservation.getProductId(),
                    reservation.getQuantity(),
                    requestedQuantity
            );
        }
    }

    @Transactional
    public InventoryReservation save(InventoryReservation reservation) {

        return repository.save(reservation);
    }
}