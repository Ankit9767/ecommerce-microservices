package com.example.inventory_service.kafka;

import com.ecommerce.common.events.InventoryEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class StockEventProducer {

    private static final String INVENTORY_UPDATED_TOPIC =
            KafkaTopics.INVENTORY_UPDATED;

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    private final Counter publishedCounter;

    public StockEventProducer(KafkaTemplate<String, InventoryEvent> kafkaTemplate,
                              MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter =
                Counter.builder("kafka.stock.events.published")
                        .description(
                                "Number of stock events successfully published to Kafka"
                        )
                        .tag("topic", INVENTORY_UPDATED_TOPIC)
                        .register(meterRegistry);
    }

    public CompletableFuture<?> publish(InventoryEvent event) {

        String key = event.getProductId().toString();

        return kafkaTemplate
                .send(
                        INVENTORY_UPDATED_TOPIC,
                        key,
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {

                        log.error(
                                "Failed to publish inventory event: " +
                                        "eventType={}, productId={}, topic={}",
                                event.getEventType(),
                                key,
                                INVENTORY_UPDATED_TOPIC,
                                throwable
                        );

                        return;
                    }

                    publishedCounter.increment();
                });
    }
}