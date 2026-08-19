package com.ecommerce.common.exception;

import lombok.Getter;

@Getter
public class InventoryReleaseException extends RuntimeException {

    private final Long productId;

    private final Integer quantity;

    public InventoryReleaseException(Long productId,
                                     Integer quantity) {

        super(
                "Failed to release inventory for product "
                        + productId
                        + " quantity "
                        + quantity
        );

        this.productId = productId;
        this.quantity = quantity;
    }

}
