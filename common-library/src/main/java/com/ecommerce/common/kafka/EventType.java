package com.ecommerce.common.kafka;

import com.ecommerce.common.events.CartAbandonedEvent;
import com.ecommerce.common.events.CartCheckedOutEvent;
import com.ecommerce.common.events.CartEvent;
import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.events.InventoryEvent;
import com.ecommerce.common.events.OrderCancelledEvent;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.events.OutOfStockEvent;
import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.events.PaymentEvent;
import com.ecommerce.common.events.ProductCreatedEvent;
import com.ecommerce.common.events.ProductDeletedEvent;
import com.ecommerce.common.events.ProductEvent;
import com.ecommerce.common.events.ProductUpdatedEvent;
import com.ecommerce.common.events.StockReleasedEvent;
import com.ecommerce.common.events.StockReservedEvent;
import com.ecommerce.common.events.StockUpdatedEvent;
import com.ecommerce.common.events.UserBlockedEvent;
import com.ecommerce.common.events.UserDeletedEvent;
import com.ecommerce.common.events.UserEvent;
import com.ecommerce.common.events.UserRegisteredEvent;
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

    /**
     * Concrete event class for this discriminator, used by the shared outbox
     * scheduler to deserialize a stored payload without a per-service switch.
     */
    public Class<? extends DomainEvent> getEventClass() {

        return switch (this) {
            case ORDER_CREATED -> OrderCreatedEvent.class;
            case ORDER_CANCELLED -> OrderCancelledEvent.class;
            case PAYMENT_SUCCESSFUL, PAYMENT_FAILED -> PaymentCompletedEvent.class;
            case STOCK_RESERVED -> StockReservedEvent.class;
            case STOCK_RELEASED -> StockReleasedEvent.class;
            case STOCK_UPDATED -> StockUpdatedEvent.class;
            case OUT_OF_STOCK -> OutOfStockEvent.class;
            case PRODUCT_CREATED -> ProductCreatedEvent.class;
            case PRODUCT_UPDATED -> ProductUpdatedEvent.class;
            case PRODUCT_DELETED -> ProductDeletedEvent.class;
            case USER_REGISTERED -> UserRegisteredEvent.class;
            case USER_DELETED -> UserDeletedEvent.class;
            case USER_BLOCKED -> UserBlockedEvent.class;
            case CART_ABANDONED -> CartAbandonedEvent.class;
            case CART_CHECKED_OUT -> CartCheckedOutEvent.class;
        };
    }
}