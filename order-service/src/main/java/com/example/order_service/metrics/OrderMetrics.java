package com.example.order_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final Counter ordersCreated;

    private final Counter ordersViewed;

    private final Counter ordersUpdated;

    private final Counter ordersCancelled;

    private final Counter orderNotFound;

    private final Counter productNotAvailable;

    private final Counter invalidStatusTransition;

    public OrderMetrics(MeterRegistry meterRegistry) {

        ordersCreated = Counter.builder("order.created")
                .description("Number of orders created")
                .register(meterRegistry);

        ordersViewed = Counter.builder("order.viewed")
                .description("Number of order lookups")
                .register(meterRegistry);

        ordersUpdated = Counter.builder("order.updated")
                .description("Number of orders updated")
                .register(meterRegistry);

        ordersCancelled = Counter.builder("order.cancelled")
                .description("Number of orders cancelled")
                .register(meterRegistry);

        orderNotFound = Counter.builder("order.not_found")
                .description("Number of order not found events")
                .register(meterRegistry);

        productNotAvailable = Counter.builder("order.product_not_available")
                .description("Number of orders rejected because a product was unavailable")
                .register(meterRegistry);

        invalidStatusTransition = Counter.builder("order.invalid_status_transition")
                .description("Number of invalid order status transition attempts")
                .register(meterRegistry);
    }

    public void orderCreated() {
        ordersCreated.increment();
    }

    public void orderViewed() {
        ordersViewed.increment();
    }

    public void orderUpdated() {
        ordersUpdated.increment();
    }

    public void orderCancelled() {
        ordersCancelled.increment();
    }

    public void orderNotFound() {
        orderNotFound.increment();
    }

    public void productNotAvailable() {
        productNotAvailable.increment();
    }

    public void invalidStatusTransition() {
        invalidStatusTransition.increment();
    }
}