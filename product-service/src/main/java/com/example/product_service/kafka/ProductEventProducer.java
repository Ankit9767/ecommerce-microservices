package com.example.product_service.kafka;

import com.ecommerce.common.events.ProductEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    private final MeterRegistry meterRegistry;

    public void publish(ProductEvent event) {

        log.info("Publishing product event '{}' for product {}",
                event.getEventType(), event.getProductId());

        kafkaTemplate.send(
                com.ecommerce.common.kafka.KafkaTopics.NOTIFICATION_SENT,
                event.getProductId().toString(),
                event
        );

        Counter.builder("kafka.product.events.published")
                .description("Product events published to Kafka")
                .register(meterRegistry)
                .increment();
    }
}