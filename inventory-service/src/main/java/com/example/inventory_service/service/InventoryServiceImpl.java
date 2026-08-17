package com.example.inventory_service.service;

import com.ecommerce.common.dto.InventoryResponse;
import com.example.inventory_service.dto.CreateInventoryRequest;
import com.example.inventory_service.dto.InventoryQuantityRequest;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {

        if (inventoryRepository.existsByProductId(request.productId())) {

            throw new InventoryAlreadyExistsException(
                    request.productId()
            );
        }

        Inventory inventory = Inventory.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .reservedQuantity(0)
                .build();

        Inventory saved = inventoryRepository.save(inventory);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long productId) {

        Inventory inventory = getInventoryOrThrow(productId);

        return toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse increaseStock(Long productId,
                                           InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.increaseStock(request.quantity());

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse decreaseStock(Long productId,
                                           InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.decreaseStock(request.quantity());

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse reserveStock(Long productId,
                                          InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.reserveStock(request.quantity());

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse releaseStock(Long productId, InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.releaseStock(request.quantity());

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse confirmReservation(Long productId,
                                                InventoryQuantityRequest request) {

        Inventory inventory = getInventoryOrThrow(productId);

        inventory.confirmReservation(request.quantity());

        return toResponse(inventoryRepository.save(inventory));
    }

    private Inventory getInventoryOrThrow(Long productId) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );
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
}