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
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public InventoryResponse createInventory(@Valid @RequestBody
                                                 CreateInventoryRequest request) {

        return inventoryService.createInventory(request);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN')")
    public InventoryResponse getInventory(
            @PathVariable Long productId) {

        return inventoryService.getInventory(productId);
    }

    @PostMapping("/{productId}/increase")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public InventoryResponse increaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.increaseStock(
                productId,
                request
        );
    }

    @PostMapping("/{productId}/decrease")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public InventoryResponse decreaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.decreaseStock(
                productId,
                request
        );
    }

    @PostMapping("/{productId}/reserve")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public InventoryResponse reserveStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.reserveStock(
                productId,
                request
        );
    }

    @PostMapping("/{productId}/release")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public InventoryResponse releaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.releaseStock(
                productId,
                request
        );
    }

    @PostMapping("/{productId}/confirm")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public InventoryResponse confirmReservation(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return inventoryService.confirmReservation(
                productId,
                request
        );
    }
}
