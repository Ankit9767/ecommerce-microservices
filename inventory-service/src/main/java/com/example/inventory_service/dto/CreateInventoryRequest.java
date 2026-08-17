package com.example.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryRequest(

        @NotNull
        Long productId,

        @NotNull
        @Min(0)
        Integer quantity

) {
}
