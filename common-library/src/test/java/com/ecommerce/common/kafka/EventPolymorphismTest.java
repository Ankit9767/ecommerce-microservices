package com.ecommerce.common.kafka;

import com.ecommerce.common.events.CartAbandonedEvent;
import com.ecommerce.common.events.CartEvent;
import com.ecommerce.common.events.InventoryEvent;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.OrderItemDto;
import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.events.OutOfStockEvent;
import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.events.PaymentEvent;
import com.ecommerce.common.events.ProductCreatedEvent;
import com.ecommerce.common.events.ProductEvent;
import com.ecommerce.common.events.StockReservedEvent;
import com.ecommerce.common.events.UserBlockedEvent;
import com.ecommerce.common.events.UserEvent;
import com.ecommerce.common.enums.PaymentMethod;
import com.ecommerce.common.enums.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the polymorphic event hierarchy: the concrete subclass must be
 * reconstructable from the payload's {@code eventType} discriminator alone --
 * with no Kafka {@code __TypeId__} headers present.
 */
class EventPolymorphismTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    @Test
    void reconstructsOrderCreatedFromPayload() throws Exception {

        String json = """
                { "eventType": "order-created",
                  "orderId": 1, "customerId": 2,
                  "currency": "INR",
                  "productId": 10, "quantity": 2,
                  "amount": 100.00, "paymentMethod": "CARD" }
                """;

        OrderEvent event = objectMapper.readValue(json, OrderEvent.class);

        assertThat(event).isInstanceOf(OrderCreatedEvent.class);
        OrderCreatedEvent created = (OrderCreatedEvent) event;
        assertThat(created.getOrderId()).isEqualTo(1L);
        assertThat(created.getCurrency()).isEqualTo(com.ecommerce.common.enums.Currency.INR);
        assertThat(created.getEventType()).isEqualTo(EventType.ORDER_CREATED);
    }

    @Test
    void reconstructsPaymentCompletedFromPayload() throws Exception {

        String json = """
                { "eventType": "payment-successful",
                  "paymentId": 1, "orderId": 2,
                  "amount": 100.00, "currency": "USD",
                  "paymentMethod": "CARD",
                  "transactionId": "txn-1",
                  "paymentStatus": "SUCCESS" }
                """;

        PaymentEvent event = objectMapper.readValue(json, PaymentEvent.class);

        assertThat(event).isInstanceOf(PaymentCompletedEvent.class);
        PaymentCompletedEvent completed = (PaymentCompletedEvent) event;
        assertThat(completed.getOrderId()).isEqualTo(2L);
        assertThat(completed.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void reconstructsStockReservedFromPayload() throws Exception {

        String json = """
                { "eventType": "stock-reserved",
                  "productId": 10, "quantity": 5,
                  "availableQuantity": 95, "reservedQuantity": 5,
                  "orderItemId": 7 }
                """;

        InventoryEvent event = objectMapper.readValue(json, InventoryEvent.class);

        assertThat(event).isInstanceOf(StockReservedEvent.class);
        assertThat(event.getProductId()).isEqualTo(10L);
    }

    @Test
    void reconstructsCartEventFromPayload() throws Exception {

        String json = """
                { "eventType": "cart-abandoned", "customerId": 42 }
                """;

        CartEvent event = objectMapper.readValue(json, CartEvent.class);

        assertThat(event).isInstanceOf(CartAbandonedEvent.class);
        assertThat(event.getCustomerId()).isEqualTo(42L);
    }

    @Test
    void reconstructsProductAndUserEventsFromPayload() throws Exception {

        ProductEvent product =
                objectMapper.readValue(
                        """
                                { "eventType": "product-created", "productId": 9,
                                  "name": "iPhone", "sku": "APL15",
                                  "price": 100.00, "active": true }
                                """,
                        ProductEvent.class);

        assertThat(product).isInstanceOf(ProductCreatedEvent.class);
        assertThat(product.getProductId()).isEqualTo(9L);

        UserEvent user =
                objectMapper.readValue(
                        """
                                { "eventType": "user-blocked", "userId": 3,
                                  "email": "x@example.com", "username": "x" }
                                """,
                        UserEvent.class);

        assertThat(user).isInstanceOf(UserBlockedEvent.class);
        assertThat(user.getUserId()).isEqualTo(3L);
    }

    @Test
    void serializesConcreteEventWithoutExtraDiscriminatorField() throws Exception {

        String json = objectMapper.writeValueAsString(
                OrderCreatedEvent.builder()
                        .eventType(EventType.ORDER_CREATED)
                        .orderId(1L)
                        .customerId(2L)
                        .paymentMethod(PaymentMethod.CARD)
                        .totalAmount(new BigDecimal("100.00"))
                        .items(List.of(
                                OrderItemDto.builder()
                                        .productId(10L)
                                        .quantity(2)
                                        .unitPrice(new BigDecimal("50.00"))
                                        .lineTotal(new BigDecimal("100.00"))
                                        .build()))
                        .build());

        // No @class / @type field sneaks into the wire format.
        assertThat(json).doesNotContain("@class").doesNotContain("\"type\"");
        // eventType remains the discriminator on the wire.
        assertThat(json).contains("\"eventType\":\"order-created\"");
        // The whole order is carried on a single event (item list, not per-item events).
        assertThat(json).contains("\"items\"");
    }

    @Test
    void unknownEventTypeFailsFastWhenTargetingAbstractBase() {

        String json = """
                { "eventType": "unknown-thing", "orderId": 1 }
                """;

        // Previously an unknown eventType deserialized to a null bundle and was
        // skipped by consumers. With explicit polymorphism the discriminator is
        // authoritative: an unknown value fails fast instead of silently
        // producing a meaningless base object.
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        objectMapper.readValue(json, OrderEvent.class))
                .isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidTypeIdException.class);
    }
}