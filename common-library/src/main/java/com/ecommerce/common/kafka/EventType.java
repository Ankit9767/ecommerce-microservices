package com.ecommerce.common.kafka;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum EventType {

    ORDER_CREATED("order-created"),
    ORDER_CANCELLED("order-cancelled"),

    PAYMENT_SUCCESSFUL("payment-successful"),
    PAYMENT_FAILED("payment-failed"),

    STOCK_RESERVED("stock-reserved"),
    STOCK_RELEASED("stock-released"),
    STOCK_UPDATED("stock-updated"),
    OUT_OF_STOCK("out-of-stock"),

    PRODUCT_CREATED("product-created"),
    PRODUCT_UPDATED("product-updated"),
    PRODUCT_DELETED("product-deleted"),

    USER_REGISTERED("user-registered"),
    USER_DELETED("user-deleted"),
    USER_BLOCKED("user-blocked"),

    CART_ABANDONED("cart-abandoned"),
    CART_CHECKED_OUT("cart-checked-out");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static EventType fromValue(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}