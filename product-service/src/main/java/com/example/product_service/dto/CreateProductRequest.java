package com.example.product_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(
            max = 150,
            message = "Product name must not exceed 150 characters"
    )
    private String name;

    @Size(
            max = 2000,
            message = "Description must not exceed 2000 characters"
    )
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.01",
            message = "Price must be greater than zero"
    )
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    @Size(
            max = 100,
            message = "Category must not exceed 100 characters"
    )
    private String category;

    @NotBlank(message = "SKU is required")
    @Size(
            max = 100,
            message = "SKU must not exceed 100 characters"
    )
    private String sku;
}