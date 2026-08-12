package com.example.product_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private String category;

    private String sku;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}