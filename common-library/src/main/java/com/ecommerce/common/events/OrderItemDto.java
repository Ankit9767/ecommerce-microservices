package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OrderItemDto {

    private Long productId;

    private String productName;

    private String sku;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal lineTotal;
}