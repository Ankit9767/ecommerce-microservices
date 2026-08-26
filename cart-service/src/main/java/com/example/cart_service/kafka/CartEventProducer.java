package com.example.cart_service.kafka;

import com.ecommerce.common.events.CartEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CartEventProducer {

    private static final String CART_EVENT_TOPIC = KafkaTopics.NOTIFICATION_SENT;

    private final KafkaTemplate<String, CartEvent> kafkaTemplate;

    private final Counter publishedCounter;

    public CartEventProducer(KafkaTemplate<String, CartEvent> kafkaTemplate,
                             MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter = Counter.builder("kafka.cart.events.published")
                .description("Number of cart events successfully published to Kafka")
                .tag("topic", CART_EVENT_TOPIC)
                .register(meterRegistry);
    }

    public CompletableFuture<?> publish(CartEvent event) {

        String key = event.getCustomerId() != null
                ? event.getCustomerId().toString()
                : "cart";

        log.debug(
                "Publishing cart event: eventType={}, key={}, topic={}",
                event.getEventType(),
                key,
                CART_EVENT_TOPIC
        );

        return kafkaTemplate
                .send(
                        CART_EVENT_TOPIC,
                        key,
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {
                        log.error(
                                "Failed to publish cart event: eventType={}, key={}, topic={}",
                                event.getEventType(),
                                key,
                                CART_EVENT_TOPIC,
                                throwable
                        );
                        return;
                    }

                    publishedCounter.increment();

                    log.debug(
                            "Successfully published cart event: eventType={}, key={}, topic={}, partition={}, offset={}",
                            event.getEventType(),
                            key,
                            CART_EVENT_TOPIC,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}