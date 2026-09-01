package com.example.product_service.kafka;

import com.ecommerce.common.events.ProductEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    private final Counter publishedCounter;

    public ProductEventProducer(KafkaTemplate<String, ProductEvent> kafkaTemplate,
                                MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter =
                Counter.builder("kafka.product.events.published")
                        .description(
                                "Number of product events successfully published to Kafka"
                        )
                        .tag("topic", KafkaTopics.PRODUCT_EVENTS)
                        .register(meterRegistry);
    }

    public CompletableFuture<?> publish(ProductEvent event) {

        String key = event.getProductId().toString();

        log.info(
                "Publishing product event: eventType={}, productId={}, topic={}",
                event.getEventType(),
                event.getProductId(),
                KafkaTopics.PRODUCT_EVENTS
        );

        return kafkaTemplate
                .send(
                        KafkaTopics.PRODUCT_EVENTS,
                        key,
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {

                        log.error(
                                "Failed to publish product event: " +
                                        "eventType={}, productId={}, topic={}",
                                event.getEventType(),
                                event.getProductId(),
                                KafkaTopics.PRODUCT_EVENTS,
                                throwable
                        );

                        return;
                    }

                    publishedCounter.increment();

                    log.info(
                            "Product event published successfully: " +
                                    "eventType={}, productId={}, topic={}, partition={}, offset={}",
                            event.getEventType(),
                            event.getProductId(),
                            KafkaTopics.PRODUCT_EVENTS,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}