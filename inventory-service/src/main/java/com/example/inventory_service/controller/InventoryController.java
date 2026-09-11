package com.example.inventory_service.controller;

import com.ecommerce.common.dto.InventoryResponse;
import com.ecommerce.common.dto.InventoryQuantityRequest;
import com.example.inventory_service.dto.CreateInventoryRequest;
import com.example.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public InventoryResponse createInventory(@Valid @RequestBody
                                                 CreateInventoryRequest request) {

        return inventoryService.createInventory(request);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public InventoryResponse getInventory(
            @PathVariable Long productId) {

        return inventoryService.getInventory(productId);
    }

    @PostMapping("/{productId}/increase")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public InventoryResponse increaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.increaseStock(
                productId,
                request
        );
    }

    @PostMapping("/{productId}/decrease")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public InventoryResponse decreaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.decreaseStock(
                productId,
                request
        );
    }

    @PostMapping("/{productId}/reserve")
    @PreAuthorize("hasRole('ROLE_INTERNAL_SERVICE')")
    public InventoryResponse reserveStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.reserveStock(
                productId,
                request
        );
    }

    @PostMapping("/{productId}/release")
    @PreAuthorize("hasRole('ROLE_INTERNAL_SERVICE')")
    public InventoryResponse releaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.releaseStock(
                productId,
                request
        );
    }

    @PostMapping("/{productId}/confirm")
    @PreAuthorize("hasRole('ROLE_INTERNAL_SERVICE')")
    public InventoryResponse confirmReservation(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.confirmReservation(
                productId,
                request
        );
    }
}
