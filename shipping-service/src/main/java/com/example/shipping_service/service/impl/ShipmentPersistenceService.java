package com.example.shipping_service.service.impl;

import com.example.shipping_service.dto.ShipmentCreationResult;
import com.example.shipping_service.entity.Shipment;
import com.example.shipping_service.enums.ShipmentStatus;
import com.example.shipping_service.exception.ShipmentNotFoundException;
import com.example.shipping_service.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentPersistenceService {

    private final ShipmentRepository repository;

    @Transactional
    public ShipmentCreationResult createIfAbsent(Long orderId,
                                                 Long customerId) {

        return repository.findByOrderId(orderId)

                .map(shipment ->
                        new ShipmentCreationResult(
                                shipment,
                                false
                        )
                )

                .orElseGet(() -> {

                    Shipment shipment =
                            Shipment.builder()
                                    .orderId(orderId)
                                    .customerId(customerId)
                                    .status(
                                            ShipmentStatus.CREATED
                                    )
                                    .build();

                    Shipment saved =
                            repository.saveAndFlush(
                                    shipment
                            );

                    return new ShipmentCreationResult(
                            saved,
                            true
                    );
                });
    }

    @Transactional(readOnly = true)
    public Shipment find(Long shipmentId) {

        return repository.findById(shipmentId)
                .orElseThrow(() ->
                        new ShipmentNotFoundException(
                                shipmentId
                        )
                );
    }

    @Transactional(readOnly = true)
    public Shipment findByOrderId(Long orderId) {

        return repository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new ShipmentNotFoundException(
                                orderId
                        )
                );
    }

    @Transactional
    public Shipment save(Shipment shipment) {

        return repository.saveAndFlush(shipment);
    }
}