package com.example.inventory_service.service.impl;

import com.ecommerce.common.dto.InventoryResponse;
import com.ecommerce.common.dto.ProductResponse;
import com.ecommerce.common.events.OutOfStockEvent;
import com.ecommerce.common.events.StockReleasedEvent;
import com.ecommerce.common.events.StockReservedEvent;
import com.ecommerce.common.events.StockUpdatedEvent;
import com.ecommerce.common.exception.RemoteResourceNotFoundException;
import com.ecommerce.common.kafka.EventType;
import com.example.inventory_service.client.ProductClient;
import com.example.inventory_service.dto.CreateInventoryRequest;
import com.ecommerce.common.dto.InventoryQuantityRequest;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.exception.InventoryAlreadyExistsException;
import com.example.inventory_service.exception.InsufficientInventoryException;
import com.example.inventory_service.exception.InventoryNotFoundException;
import com.example.inventory_service.exception.ProductNotAvailableException;
import com.example.inventory_service.mapper.InventoryMapper;
import com.example.inventory_service.metrics.InventoryMetrics;
import com.example.inventory_service.repository.InventoryRepository;
import com.example.inventory_service.service.InventoryService;
import com.example.inventory_service.service.OutboxService;
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

    private final OutboxService outboxService;

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

        outboxService.saveStockUpdatedEvent(
                StockUpdatedEvent.builder()
                        .eventType(EventType.STOCK_UPDATED)
                        .productId(response.productId())
                        .quantity(response.quantity())
                        .availableQuantity(response.availableQuantity())
                        .reservedQuantity(response.reservedQuantity())
                        .build()
        );

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

        outboxService.saveStockUpdatedEvent(
                StockUpdatedEvent.builder()
                        .eventType(EventType.STOCK_UPDATED)
                        .productId(response.productId())
                        .quantity(response.quantity())
                        .availableQuantity(response.availableQuantity())
                        .reservedQuantity(response.reservedQuantity())
                        .build()
        );

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

            outboxService.saveOutOfStockEvent(
                    OutOfStockEvent.builder()
                            .eventType(EventType.OUT_OF_STOCK)
                            .productId(productId)
                            .quantity(request.quantity())
                            .availableQuantity(
                                    inventory.getAvailableQuantity()
                            )
                            .requestedQuantity(
                                    request.quantity()
                            )
                            .build()
            );

            throw ex;
        }

        InventoryResponse response =
                inventoryPersistenceService.save(
                        inventory,
                        productId
                );

        inventoryMetrics.stockReserved();

        outboxService.saveStockReservedEvent(
                StockReservedEvent.builder()
                        .eventType(EventType.STOCK_RESERVED)
                        .productId(response.productId())
                        .quantity(response.quantity())
                        .availableQuantity(response.availableQuantity())
                        .reservedQuantity(response.reservedQuantity())
                        .build()
        );

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

        outboxService.saveStockReleasedEvent(
                StockReleasedEvent.builder()
                        .eventType(EventType.STOCK_RELEASED)
                        .productId(response.productId())
                        .quantity(response.quantity())
                        .availableQuantity(response.availableQuantity())
                        .reservedQuantity(response.reservedQuantity())
                        .build()
        );

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

        outboxService.saveStockUpdatedEvent(
                StockUpdatedEvent.builder()
                        .eventType(EventType.STOCK_UPDATED)
                        .productId(response.productId())
                        .quantity(response.quantity())
                        .availableQuantity(response.availableQuantity())
                        .reservedQuantity(response.reservedQuantity())
                        .build()
        );

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