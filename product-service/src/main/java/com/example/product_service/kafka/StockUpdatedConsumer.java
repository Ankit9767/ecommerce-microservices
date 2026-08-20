package com.example.product_service.kafka;

import com.ecommerce.common.events.InventoryEvent;
import com.ecommerce.common.events.StockUpdatedEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.product_service.repository.ProductRepository;
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
public class StockUpdatedConsumer {

    private final ProductRepository productRepository;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.INVENTORY_UPDATED,
            groupId = "product-group"
    )
    public void consume(InventoryEvent event) {

        log.info("Received InventoryEvent [{}] for product {}",
                event.getEventType(), event.getProductId());

        if (!(event instanceof StockUpdatedEvent)) {
            return;
        }

        productRepository.findById(event.getProductId())
                .ifPresentOrElse(product -> {
                    product.setStockQuantity(
                            event.getAvailableQuantity() != null
                                    ? event.getAvailableQuantity()
                                    : event.getQuantity()
                    );
                    productRepository.save(product);
                    log.info("Updated stock quantity for product {} to {}",
                            product.getId(), product.getStockQuantity());
                }, () -> log.warn("No product found for stock update, productId={}",
                        event.getProductId()));
    }

    @DltHandler
    public void handleDeadLetter(InventoryEvent event) {

        log.error("Inventory event moved to DLT after retries exhausted : {}", event);
    }
}