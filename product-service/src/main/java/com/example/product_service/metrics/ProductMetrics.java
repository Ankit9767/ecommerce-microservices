package com.example.product_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ProductMetrics {

    private final Counter productsCreated;

    private final Counter productsViewed;

    private final Counter productsDeactivated;

    private final Counter productNotFound;

    private final Counter duplicateSku;

    public ProductMetrics(MeterRegistry meterRegistry) {

        productsCreated = Counter.builder("product.created")
                .description("Number of products created")
                .register(meterRegistry);

        productsViewed = Counter.builder("product.viewed")
                .description("Number of product lookups")
                .register(meterRegistry);

        productsDeactivated = Counter.builder("product.deactivated")
                .description("Number of products deactivated")
                .register(meterRegistry);

        productNotFound = Counter.builder("product.not_found")
                .description("Number of product not found events")
                .register(meterRegistry);

        duplicateSku = Counter.builder("product.duplicate_sku")
                .description("Number of duplicate SKU attempts")
                .register(meterRegistry);
    }

    public void productCreated() {
        productsCreated.increment();
    }

    public void productViewed() {
        productsViewed.increment();
    }

    public void productDeactivated() {
        productsDeactivated.increment();
    }

    public void productNotFound() {
        productNotFound.increment();
    }

    public void duplicateSku() {
        duplicateSku.increment();
    }
}