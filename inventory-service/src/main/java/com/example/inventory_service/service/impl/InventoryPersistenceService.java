package com.example.inventory_service.service.impl;

import com.ecommerce.common.dto.InventoryResponse;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.exception.InventoryAlreadyExistsException;
import com.example.inventory_service.exception.InventoryConcurrentModificationException;
import com.example.inventory_service.mapper.InventoryMapper;
import com.example.inventory_service.metrics.InventoryMetrics;
import com.example.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryPersistenceService {

    private final InventoryRepository inventoryRepository;

    private final InventoryMetrics inventoryMetrics;

    private final InventoryMapper inventoryMapper;

    @Transactional
    public InventoryResponse save(Inventory inventory,
                                  Long productId) {

        Inventory saved =
                saveInventory(
                        inventory,
                        productId
                );

        return inventoryMapper.toResponse(saved);
    }

    @Transactional
    public InventoryResponse create(Inventory inventory,
                                    Long productId) {

        Inventory saved =
                saveInventory(
                        inventory,
                        productId
                );

        return inventoryMapper.toResponse(saved);
    }

    private Inventory saveInventory(Inventory inventory,
                                    Long productId) {

        try {

            return inventoryRepository.saveAndFlush(inventory);

        } catch (ObjectOptimisticLockingFailureException ex) {

            inventoryMetrics.concurrentModification();

            throw new InventoryConcurrentModificationException(
                    productId
            );

        } catch (DataIntegrityViolationException ex) {

            inventoryMetrics.inventoryAlreadyExists();

            throw new InventoryAlreadyExistsException(
                    productId
            );
        }
    }
}