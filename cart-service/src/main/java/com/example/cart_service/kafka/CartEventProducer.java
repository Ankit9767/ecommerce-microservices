package com.example.cart_service.kafka;

import com.ecommerce.common.events.CartEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publish-only producer for cart life-cycle events.
 *
 * <p>{@link KafkaTopics#NOTIFICATION_SENT} is a coarse analytics/marketing
 * topic - no consumer exists yet in this milestone; the future NOTIFICATION
 * module will own them.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventProducer {

    private final KafkaTemplate<String, CartEvent> kafkaTemplate;

    private final MeterRegistry meterRegistry;

    public void publish(CartEvent event) {

        log.info("Publishing cart event '{}'", event.getEventType());

        kafkaTemplate.send(
                com.ecommerce.common.kafka.KafkaTopics.NOTIFICATION_SENT,
                event.getCustomerId() != null
                        ? event.getCustomerId().toString()
                        : "cart",
                event
        );

        Counter.builder("kafka.cart.events.published")
                .description("Cart events published to Kafka")
                .register(meterRegistry)
                .increment();
    }
}