package com.example.order_service.exception;

import com.ecommerce.common.enums.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(OrderStatus current,
                                                 OrderStatus target) {

        super(
                "Cannot change order status from "
                        + current
                        + " to "
                        + target
        );
    }
}