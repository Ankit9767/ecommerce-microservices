package com.ecommerce.common.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the on-the-wire Kafka format: the EventType enum must keep serializing
 * to the same stable kebab-case strings it did before the enum refactor, and
 * must tolerate unknown values that older consumers may not know yet.
 */
class EventTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesToStableKebabCaseValues() throws Exception {

        assertThat(serialize(EventType.ORDER_CREATED)).isEqualTo("order-created");
        assertThat(serialize(EventType.ORDER_CANCELLED)).isEqualTo("order-cancelled");
        assertThat(serialize(EventType.PAYMENT_SUCCESSFUL)).isEqualTo("payment-successful");
        assertThat(serialize(EventType.PAYMENT_FAILED)).isEqualTo("payment-failed");
        assertThat(serialize(EventType.STOCK_RESERVED)).isEqualTo("stock-reserved");
        assertThat(serialize(EventType.STOCK_RELEASED)).isEqualTo("stock-released");
        assertThat(serialize(EventType.STOCK_UPDATED)).isEqualTo("stock-updated");
        assertThat(serialize(EventType.OUT_OF_STOCK)).isEqualTo("out-of-stock");
        assertThat(serialize(EventType.PRODUCT_CREATED)).isEqualTo("product-created");
        assertThat(serialize(EventType.USER_REGISTERED)).isEqualTo("user-registered");
        assertThat(serialize(EventType.CART_CHECKED_OUT)).isEqualTo("cart-checked-out");
    }

    @Test
    void deserializesFromStableKebabCaseValues() throws Exception {

        assertThat(deserialize("order-created")).isEqualTo(EventType.ORDER_CREATED);
        assertThat(deserialize("payment-successful"))
                .isEqualTo(EventType.PAYMENT_SUCCESSFUL);
        assertThat(deserialize("out-of-stock")).isEqualTo(EventType.OUT_OF_STOCK);
    }

    @Test
    void unknownValueMapsToNullGracefully() throws Exception {

        assertThat(deserialize("unknown-future-event")).isNull();
        assertThat(deserialize(null)).isNull();
        assertThat(deserialize("")).isNull();
    }

    @Test
    void roundTripsInsideAnEvent() throws Exception {

        OrderEventCarrier carrier = new OrderEventCarrier(EventType.ORDER_CREATED);

        String json = objectMapper.writeValueAsString(carrier);

        assertThat(json).contains("\"eventType\":\"order-created\"");

        OrderEventCarrier restored =
                objectMapper.readValue(json, OrderEventCarrier.class);

        assertThat(restored.getEventType()).isEqualTo(EventType.ORDER_CREATED);
    }

    private String serialize(EventType type) throws Exception {
        return objectMapper.writeValueAsString(type).replace("\"", "");
    }

    private EventType deserialize(String value) throws Exception {
        return objectMapper.readValue(
                value == null ? "null" : "\"" + value + "\"",
                EventType.class
        );
    }

    public static class OrderEventCarrier {
        private EventType eventType;

        public OrderEventCarrier() {
        }

        public OrderEventCarrier(EventType eventType) {
            this.eventType = eventType;
        }

        public EventType getEventType() {
            return eventType;
        }

        public void setEventType(EventType eventType) {
            this.eventType = eventType;
        }
    }
}