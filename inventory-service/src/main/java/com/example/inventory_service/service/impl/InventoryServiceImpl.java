package com.example.inventory_service.service.impl;

import com.ecommerce.common.dto.InventoryResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.exception.RemoteResourceNotFoundException;
import com.example.inventory_service.client.ProductClient;
import com.example.inventory_service.dto.CreateInventoryRequest;
import com.ecommerce.common.dto.InventoryQuantityRequest;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.exception.InventoryAlreadyExistsException;
import com.example.inventory_service.exception.InsufficientInventoryException;
import com.example.inventory_service.exception.InventoryNotFoundException;
import com.example.inventory_service.exception.ProductNotAvailableException;
import com.example.inventory_service.kafka.StockEventProducer;
import com.example.inventory_service.mapper.InventoryMapper;
import com.example.inventory_service.metrics.InventoryMetrics;
import com.example.inventory_service.repository.InventoryRepository;
import com.example.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    private final InventoryMetrics inventoryMetrics;

    private final ProductClient productClient;

    private final InventoryPersistenceService inventoryPersistenceService;

    private final InventoryMapper inventoryMapper;

    private final StockEventProducer stockEventProducer;

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

        InventoryResponse response =
                inventoryPersistenceService.create(
                        inventory,
                        request.productId()
                );

        inventoryMetrics.inventoryCreated();

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long productId) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventoryMetrics.inventoryViewed();

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse increaseStock(Long productId,
                                           InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.increaseStock(request.quantity());

        InventoryResponse response =
                inventoryPersistenceService.save(
                        inventory,
                        productId
                );

        inventoryMetrics.stockIncreased();

        stockEventProducer.stockUpdated(response);

        return response;
    }

    @Override
    @Transactional
    public InventoryResponse decreaseStock(Long productId,
                                           InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.decreaseStock(request.quantity());

        InventoryResponse response =
                inventoryPersistenceService.save(
                        inventory,
                        productId
                );

        inventoryMetrics.stockDecreased();

        stockEventProducer.stockUpdated(response);

        return response;
    }

    @Override
    @Transactional
    public InventoryResponse reserveStock(Long productId,
                                          InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        try {

            inventory.reserveStock(request.quantity());

        } catch (InsufficientInventoryException ex) {

            inventoryMetrics.insufficientInventory();

            stockEventProducer.outOfStock(
                    productId,
                    request.quantity(),
                    inventory.getAvailableQuantity()
            );

            throw ex;
        }

        InventoryResponse response =
                inventoryPersistenceService.save(
                        inventory,
                        productId
                );

        inventoryMetrics.stockReserved();

        stockEventProducer.stockReserved(response);

        return response;
    }

    @Override
    @Transactional
    public InventoryResponse releaseStock(Long productId, InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.releaseStock(request.quantity());

        InventoryResponse response =
                inventoryPersistenceService.save(
                        inventory,
                        productId
                );

        inventoryMetrics.stockReleased();

        stockEventProducer.stockReleased(response);

        return response;
    }

    @Override
    @Transactional
    public InventoryResponse confirmReservation(Long productId,
                                                InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.confirmReservation(request.quantity());

        InventoryResponse response =
                inventoryPersistenceService.save(
                        inventory,
                        productId
                );

        inventoryMetrics.reservationConfirmed();

        stockEventProducer.stockUpdated(response);

        return response;
    }

    private Inventory getInventoryOrThrow(Long productId) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> {

                    inventoryMetrics.inventoryNotFound();

                    return new InventoryNotFoundException(productId);
                });
    }
}