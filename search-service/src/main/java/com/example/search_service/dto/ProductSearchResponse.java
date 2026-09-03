package com.example.search_service.dto;

import com.example.search_service.document.ProductDocument;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductSearchResponse {

    private Long productId;
    private String name;
    private String sku;
    private String category;
    private BigDecimal price;
    private Boolean active;

    public static ProductSearchResponse from(ProductDocument product) {
        return ProductSearchResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .sku(product.getSku())
                .category(product.getCategory())
                .price(product.getPrice())
                .active(product.getActive())
                .build();
    }
}