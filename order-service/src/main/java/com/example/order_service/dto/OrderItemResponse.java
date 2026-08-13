package com.example.order_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;

    private Long productId;

    private String productName;

    private String sku;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal lineTotal;
}