package com.example.product_service.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductImageUploadRequest {

    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder;

    private Boolean primaryImage = false;
}