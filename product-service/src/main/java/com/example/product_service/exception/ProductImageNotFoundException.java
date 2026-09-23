package com.example.product_service.exception;

import lombok.Getter;

@Getter
public class ProductImageNotFoundException extends RuntimeException {

    private final Long productId;
    private final Long imageId;

    public ProductImageNotFoundException(
            Long productId,
            Long imageId) {

        super(
                "Product image not found: productId="
                        + productId
                        + ", imageId="
                        + imageId
        );

        this.productId = productId;
        this.imageId = imageId;
    }
}