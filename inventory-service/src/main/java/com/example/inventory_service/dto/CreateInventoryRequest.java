package com.example.inventory_service.dto;

public record CreateInventoryRequest(
        Long productId,
        Integer quantity
) {
}
