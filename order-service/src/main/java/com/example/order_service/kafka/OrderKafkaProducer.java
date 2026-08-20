package com.example.order_service.kafka;

import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private final MeterRegistry meterRegistry;

    public void publish(OrderEvent event) {

        log.info("Publishing {} on topic {} : {}", event.getEventType(),
                KafkaTopics.ORDER_CREATED, event);

        kafkaTemplate.send(
                KafkaTopics.ORDER_CREATED,
                event.getOrderId().toString(),
                event
        );

        Counter.builder("kafka.order.events.published")
                .description("Order events published to Kafka")
                .register(meterRegistry)
                .increment();
    }
}