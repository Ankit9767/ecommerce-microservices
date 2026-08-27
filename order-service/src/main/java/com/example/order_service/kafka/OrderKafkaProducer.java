package com.example.order_service.kafka;

import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private final Counter publishedCounter;

    public OrderKafkaProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate,
                              MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter = Counter.builder("kafka.order.events.published")
                        .description("Number of order events successfully published to Kafka")
                        .register(meterRegistry);
    }

    public CompletableFuture<?> publish(OrderEvent event) {

        String orderId = event.getOrderId().toString();

        String topic = resolveTopic(event.getEventType());

        log.debug(
                "Publishing order event: eventType={}, orderId={}, topic={}",
                event.getEventType(),
                orderId,
                topic
        );

        return kafkaTemplate
                .send(
                        topic,
                        orderId,
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {

                        log.error(
                                "Failed to publish order event: " +
                                        "eventType={}, orderId={}, topic={}",
                                event.getEventType(),
                                orderId,
                                topic,
                                throwable
                        );

                        return;
                    }

                    publishedCounter.increment();

                    log.debug(
                            "Successfully published order event: " +
                                    "eventType={}, orderId={}, topic={}, " +
                                    "partition={}, offset={}",
                            event.getEventType(),
                            orderId,
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }

    private String resolveTopic(EventType eventType) {

        return switch (eventType) {

            case ORDER_CREATED ->
                    KafkaTopics.ORDER_CREATED;

            case ORDER_CANCELLED ->
                    KafkaTopics.ORDER_CANCELLED;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported order event type: " + eventType
                    );
        };
    }
}