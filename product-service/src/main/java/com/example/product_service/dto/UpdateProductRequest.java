package com.example.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProductRequest {

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
    @Digits(
            integer = 17,
            fraction = 2,
            message = "Price must have at most 17 integer digits and 2 decimal places"
    )
    private BigDecimal price;

    @NotNull(message = "Category is required")
    private Long categoryId;
}