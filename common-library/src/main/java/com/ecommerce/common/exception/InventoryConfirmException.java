package com.ecommerce.common.exception;

import lombok.Getter;

@Getter
public class InventoryConfirmException extends RuntimeException {

    private final Long productId;

    private final Integer quantity;

    public InventoryConfirmException(Long productId,
                                     Integer quantity) {

        super(
                "Failed to confirm inventory for product "
                        + productId
                        + " quantity "
                        + quantity
        );

        this.productId = productId;
        this.quantity = quantity;
    }

}