package com.example.inventory_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class InventoryMetrics {

    private final Counter inventoryCreated;

    private final Counter inventoryViewed;

    private final Counter stockIncreased;

    private final Counter stockDecreased;

    private final Counter stockReserved;

    private final Counter stockReleased;

    private final Counter reservationConfirmed;

    private final Counter inventoryNotFound;

    private final Counter inventoryAlreadyExists;

    private final Counter insufficientInventory;

    private final Counter invalidInventoryOperation;

    private final Counter concurrentModification;

    private final Counter productNotAvailable;

    private final Counter reservationDuplicateCounter;


    public InventoryMetrics(
            MeterRegistry meterRegistry
    ) {

        inventoryCreated =
                Counter.builder("inventory.created")
                        .description("Number of inventory records created")
                        .register(meterRegistry);

        inventoryViewed =
                Counter.builder("inventory.viewed")
                        .description("Number of inventory lookups")
                        .register(meterRegistry);

        stockIncreased =
                Counter.builder("inventory.stock_increased")
                        .description("Number of stock increase operations")
                        .register(meterRegistry);

        stockDecreased =
                Counter.builder("inventory.stock_decreased")
                        .description("Number of stock decrease operations")
                        .register(meterRegistry);

        stockReserved =
                Counter.builder("inventory.stock_reserved")
                        .description("Number of stock reservation operations")
                        .register(meterRegistry);

        stockReleased =
                Counter.builder("inventory.stock_released")
                        .description("Number of stock release operations")
                        .register(meterRegistry);

        reservationConfirmed =
                Counter.builder("inventory.reservation_confirmed")
                        .description("Number of confirmed reservations")
                        .register(meterRegistry);

        inventoryNotFound =
                Counter.builder("inventory.not_found")
                        .description("Number of inventory not found events")
                        .register(meterRegistry);

        inventoryAlreadyExists =
                Counter.builder("inventory.already_exists")
                        .description("Number of duplicate inventory creation attempts")
                        .register(meterRegistry);

        insufficientInventory =
                Counter.builder("inventory.insufficient")
                        .description("Number of insufficient inventory attempts")
                        .register(meterRegistry);

        invalidInventoryOperation =
                Counter.builder("inventory.invalid_operation")
                        .description("Number of invalid inventory operations")
                        .register(meterRegistry);

        concurrentModification =
                Counter.builder("inventory.concurrent_modification")
                        .description("Number of concurrent inventory modifications")
                        .register(meterRegistry);

        productNotAvailable =
                Counter.builder("inventory.product_not_available")
                        .description(
                                "Number of inventory creation attempts for unavailable products"
                        )
                        .register(meterRegistry);

        reservationDuplicateCounter =
                Counter.builder("inventory.reservation_duplicate")
                        .description("Number of duplicate inventory reservation attempts")
                        .register(meterRegistry);

    }

    public void inventoryCreated() {
        inventoryCreated.increment();
    }

    public void inventoryViewed() {
        inventoryViewed.increment();
    }

    public void stockIncreased() {
        stockIncreased.increment();
    }

    public void stockDecreased() {
        stockDecreased.increment();
    }

    public void stockReserved() {
        stockReserved.increment();
    }

    public void stockReleased() {
        stockReleased.increment();
    }

    public void reservationConfirmed() {
        reservationConfirmed.increment();
    }

    public void inventoryNotFound() {
        inventoryNotFound.increment();
    }

    public void inventoryAlreadyExists() {
        inventoryAlreadyExists.increment();
    }

    public void insufficientInventory() {
        insufficientInventory.increment();
    }

    public void invalidInventoryOperation() {
        invalidInventoryOperation.increment();
    }

    public void concurrentModification() {
        concurrentModification.increment();
    }

    public void productNotAvailable() {
        productNotAvailable.increment();
    }

    public void reservationDuplicate() {
        reservationDuplicateCounter.increment();
    }
}