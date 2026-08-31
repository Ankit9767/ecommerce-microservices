package com.ecommerce.common.kafka;

import com.ecommerce.common.events.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum EventType {

    ORDER_CREATED("order-created"),
    ORDER_CANCELLED("order-cancelled"),
    ORDER_PAID("order-paid"),

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
    CART_CHECKED_OUT("cart-checked-out"),

    SHIPMENT_CREATED("shipment-created"),
    SHIPMENT_SHIPPED("shipment-shipped"),
    SHIPMENT_IN_TRANSIT("shipment-in-transit"),
    SHIPMENT_OUT_FOR_DELIVERY("shipment-out-for-delivery"),
    SHIPMENT_DELIVERED("shipment-delivered"),
    SHIPMENT_FAILED("shipment-failed"),
    SHIPMENT_CANCELLED("shipment-cancelled");

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

    public Class<? extends DomainEvent> getEventClass() {

        return switch (this) {

            case ORDER_CREATED ->
                    OrderCreatedEvent.class;

            case ORDER_CANCELLED ->
                    OrderCancelledEvent.class;

            case ORDER_PAID ->
                    OrderPaidEvent.class;

            case PAYMENT_SUCCESSFUL,
                 PAYMENT_FAILED ->
                    PaymentCompletedEvent.class;

            case STOCK_RESERVED ->
                    StockReservedEvent.class;

            case STOCK_RELEASED ->
                    StockReleasedEvent.class;

            case STOCK_UPDATED ->
                    StockUpdatedEvent.class;

            case OUT_OF_STOCK ->
                    OutOfStockEvent.class;

            case PRODUCT_CREATED ->
                    ProductCreatedEvent.class;

            case PRODUCT_UPDATED ->
                    ProductUpdatedEvent.class;

            case PRODUCT_DELETED ->
                    ProductDeletedEvent.class;

            case USER_REGISTERED ->
                    UserRegisteredEvent.class;

            case USER_DELETED ->
                    UserDeletedEvent.class;

            case USER_BLOCKED ->
                    UserBlockedEvent.class;

            case CART_ABANDONED ->
                    CartAbandonedEvent.class;

            case CART_CHECKED_OUT ->
                    CartCheckedOutEvent.class;

            case SHIPMENT_CREATED ->
                    ShipmentCreatedEvent.class;

            case SHIPMENT_SHIPPED ->
                    ShipmentShippedEvent.class;

            case SHIPMENT_IN_TRANSIT ->
                    ShipmentInTransitEvent.class;

            case SHIPMENT_OUT_FOR_DELIVERY ->
                    ShipmentOutForDeliveryEvent.class;

            case SHIPMENT_DELIVERED ->
                    ShipmentDeliveredEvent.class;

            case SHIPMENT_FAILED ->
                    ShipmentFailedEvent.class;

            case SHIPMENT_CANCELLED ->
                    ShipmentCancelledEvent.class;
        };
    }
}
