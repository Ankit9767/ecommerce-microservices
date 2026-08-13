package com.ecommerce.common.dto;

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

    private Long categoryId;

    private String category;

    private String sku;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}