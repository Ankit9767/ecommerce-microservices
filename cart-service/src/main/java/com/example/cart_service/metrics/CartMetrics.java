package com.example.cart_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CartMetrics {

    private final Counter cartViewed;
    private final Counter itemAdded;
    private final Counter itemUpdated;
    private final Counter itemRemoved;
    private final Counter cartCleared;

    private final Counter checkoutStarted;
    private final Counter checkoutCompleted;
    private final Counter checkoutFailed;

    private final Counter emptyCheckout;
    private final Counter productUnavailable;
    private final Counter cartNotFound;
    private final Counter itemNotFound;
    private final Counter concurrentModification;

    public CartMetrics(MeterRegistry meterRegistry) {

        cartViewed = Counter.builder("cart.viewed")
                .description("Number of cart views")
                .register(meterRegistry);

        itemAdded = Counter.builder("cart.item_added")
                .description("Number of items added to carts")
                .register(meterRegistry);

        itemUpdated = Counter.builder("cart.item_updated")
                .description("Number of cart item updates")
                .register(meterRegistry);

        itemRemoved = Counter.builder("cart.item_removed")
                .description("Number of cart items removed")
                .register(meterRegistry);

        cartCleared = Counter.builder("cart.cleared")
                .description("Number of carts cleared")
                .register(meterRegistry);

        checkoutStarted = Counter.builder("cart.checkout_started")
                .description("Number of checkout attempts")
                .register(meterRegistry);

        checkoutCompleted = Counter.builder("cart.checkout_completed")
                .description("Number of successful checkouts")
                .register(meterRegistry);

        checkoutFailed = Counter.builder("cart.checkout_failed")
                .description("Number of failed checkout attempts")
                .register(meterRegistry);

        emptyCheckout = Counter.builder("cart.empty_checkout")
                .description("Number of checkout attempts with an empty cart")
                .register(meterRegistry);

        productUnavailable = Counter.builder("cart.product_unavailable")
                .description("Number of cart operations rejected because a product was unavailable")
                .register(meterRegistry);

        cartNotFound = Counter.builder("cart.not_found")
                .description("Number of cart not found events")
                .register(meterRegistry);

        itemNotFound = Counter.builder("cart.item_not_found")
                .description("Number of cart item not found events")
                .register(meterRegistry);

        concurrentModification = Counter.builder("cart.concurrent_modification")
                .description("Number of cart concurrent modification events")
                .register(meterRegistry);
    }

    public void cartViewed() {
        cartViewed.increment();
    }

    public void itemAdded() {
        itemAdded.increment();
    }

    public void itemUpdated() {
        itemUpdated.increment();
    }

    public void itemRemoved() {
        itemRemoved.increment();
    }

    public void cartCleared() {
        cartCleared.increment();
    }

    public void checkoutStarted() {
        checkoutStarted.increment();
    }

    public void checkoutCompleted() {
        checkoutCompleted.increment();
    }

    public void checkoutFailed() {
        checkoutFailed.increment();
    }

    public void emptyCheckout() {
        emptyCheckout.increment();
    }

    public void productUnavailable() {
        productUnavailable.increment();
    }

    public void cartNotFound() {
        cartNotFound.increment();
    }

    public void itemNotFound() {
        itemNotFound.increment();
    }

    public void concurrentModification() {
        concurrentModification.increment();
    }
}