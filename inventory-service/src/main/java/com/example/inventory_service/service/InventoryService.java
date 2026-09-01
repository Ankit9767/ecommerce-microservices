package com.example.inventory_service.service;

import com.ecommerce.common.dto.InventoryResponse;
import com.example.inventory_service.dto.CreateInventoryRequest;
import com.ecommerce.common.dto.InventoryQuantityRequest;

public interface InventoryService {

    InventoryResponse createInventory(CreateInventoryRequest request);

    InventoryResponse getInventory(Long productId);

    InventoryResponse increaseStock(Long productId, InventoryQuantityRequest request);

    InventoryResponse decreaseStock(Long productId, InventoryQuantityRequest request);

    InventoryResponse reserveStock(Long productId, InventoryQuantityRequest request);

    InventoryResponse releaseStock(Long productId, InventoryQuantityRequest request);

    InventoryResponse confirmReservation(Long productId, InventoryQuantityRequest request);

    void createInventoryForProduct(Long productId);
}
