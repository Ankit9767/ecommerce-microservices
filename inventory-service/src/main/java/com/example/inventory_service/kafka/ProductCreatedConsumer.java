package com.example.inventory_service.kafka;

import com.ecommerce.common.events.ProductCreatedEvent;
import com.ecommerce.common.events.ProductEvent;
import com.ecommerce.common.exception.InvalidEventException;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class ProductCreatedConsumer {

    private final InventoryService inventoryService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.PRODUCT_EVENTS,
            groupId = "inventory-product-group"
    )
    public void consume(ProductEvent event) {

        if (event == null || event.getEventType() == null) {

            throw new InvalidEventException();
        }

        log.info(
                "Received ProductEvent [{}] for product {}",
                event.getEventType(),
                event.getProductId()
        );

        switch (event.getEventType()) {

            case PRODUCT_CREATED -> {

                if (!(event instanceof ProductCreatedEvent productEvent)) {

                    log.error(
                            "Invalid PRODUCT_CREATED event type: {}",
                            event.getClass().getName()
                    );

                    throw new InvalidEventException();
                }

                inventoryService.createInventoryForProduct(
                        productEvent.getProductId()
                );

                log.info(
                        "Inventory created for product {}",
                        productEvent.getProductId()
                );
            }

            default -> log.debug(
                    "Ignoring product event type: {}",
                    event.getEventType()
            );
        }
    }

    @DltHandler
    public void handleDeadLetter(ProductEvent event) {

        log.error(
                "Product event moved to DLT after retries exhausted: {}",
                event
        );
    }
}
