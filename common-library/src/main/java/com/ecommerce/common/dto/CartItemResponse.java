package com.ecommerce.common.dto;

import java.math.BigDecimal;

public record CartItemResponse(

        Long id,

        Long productId,

        String productName,

        String sku,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal lineTotal
) {
}