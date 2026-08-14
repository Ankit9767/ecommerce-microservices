package com.example.order_service.exception;

public class OrderNotEditableException extends RuntimeException {

    public OrderNotEditableException(Long orderId) {

        super(
                "Order " + orderId +
                        " cannot be modified in its current state"
        );
    }
}
