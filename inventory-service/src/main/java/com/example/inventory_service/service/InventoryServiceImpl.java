package com.example.inventory_service.service;

import com.ecommerce.common.dto.InventoryResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.exception.RemoteResourceNotFoundException;
import com.example.inventory_service.client.ProductClient;
import com.example.inventory_service.dto.CreateInventoryRequest;
import com.example.inventory_service.dto.InventoryQuantityRequest;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.exception.InventoryAlreadyExistsException;
import com.example.inventory_service.exception.InventoryConcurrentModificationException;
import com.example.inventory_service.exception.InventoryNotFoundException;
import com.example.inventory_service.exception.ProductNotAvailableException;
import com.example.inventory_service.metrics.InventoryMetrics;
import com.example.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    private final InventoryMetrics inventoryMetrics;

    private final ProductClient productClient;

    @Override
    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {

        ProductResponse product;

        try {

            product = productClient.getProduct(request.productId());

        } catch (RemoteResourceNotFoundException ex) {

            inventoryMetrics.productNotAvailable();

            throw new ProductNotAvailableException(
                    request.productId()
            );
        }

        if (product == null || Boolean.FALSE.equals(product.getActive())) {

            inventoryMetrics.productNotAvailable();

            throw new ProductNotAvailableException(
                    request.productId()
            );
        }

        if (inventoryRepository.existsByProductId(request.productId())) {

            inventoryMetrics.inventoryAlreadyExists();

            throw new InventoryAlreadyExistsException(
                    request.productId()
            );
        }

        Inventory inventory = Inventory.builder()
                .productId(product.getId())
                .quantity(request.quantity())
                .reservedQuantity(0)
                .build();

        try {

            Inventory saved = inventoryRepository.saveAndFlush(inventory);

            inventoryMetrics.inventoryCreated();

            return toResponse(saved);

        } catch (DataIntegrityViolationException ex) {

            inventoryMetrics.inventoryAlreadyExists();

            throw new InventoryAlreadyExistsException(
                    request.productId()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long productId) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventoryMetrics.inventoryViewed();

        return toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse increaseStock(Long productId,
                                           InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.increaseStock(request.quantity());

        Inventory saved =
                saveInventory(
                        inventory,
                        productId
                );

        inventoryMetrics.stockIncreased();

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse decreaseStock(Long productId,
                                           InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.decreaseStock(request.quantity());

        Inventory saved =
                saveInventory(
                        inventory,
                        productId
                );

        inventoryMetrics.stockDecreased();

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse reserveStock(Long productId,
                                          InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.reserveStock(request.quantity());

        Inventory saved =
                saveInventory(
                        inventory,
                        productId
                );

        inventoryMetrics.stockReserved();

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse releaseStock(Long productId, InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.releaseStock(request.quantity());

        Inventory saved =
                saveInventory(
                        inventory,
                        productId
                );

        inventoryMetrics.stockReleased();

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse confirmReservation(Long productId,
                                                InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.confirmReservation(request.quantity());

        Inventory saved =
                saveInventory(
                        inventory,
                        productId
                );

        inventoryMetrics.reservationConfirmed();

        return toResponse(saved);
    }

    private Inventory getInventoryOrThrow(Long productId) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> {

                    inventoryMetrics.inventoryNotFound();

                    return new InventoryNotFoundException(productId);
                });
    }

    private InventoryResponse toResponse(Inventory inventory) {

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );
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
        }
    }
}