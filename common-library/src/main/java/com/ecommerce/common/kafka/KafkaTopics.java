package com.ecommerce.common.kafka;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order-created";

    public static final String ORDER_CANCELLED = "order-cancelled";

    public static final String ORDER_PAID = "order-paid";

    public static final String SHIPMENT_EVENTS = "shipment-events";

    public static final String PAYMENT_COMPLETED = "payment-completed";

    public static final String INVENTORY_UPDATED = "inventory-updated";

    public static final String NOTIFICATION_SENT = "notification-sent";

    /*
     * Kafka message header that carries the fine-grained event type for
     * observability.
     */
    public static final String HEADER_EVENT_TYPE = "X-Event-Type";

}