package com.example.order_service.kafka;

import com.ecommerce.common.events.OrderEvent;
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

    private static final String ORDER_CREATED_TOPIC = KafkaTopics.ORDER_CREATED;

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private final Counter publishedCounter;

    public OrderKafkaProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate,
                              MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter = Counter.builder("kafka.order.events.published")
                .description("Number of order events successfully published to Kafka")
                .tag("topic", ORDER_CREATED_TOPIC)
                .register(meterRegistry);
    }

    public CompletableFuture<?> publish(OrderEvent event) {

        String orderId = event.getOrderId().toString();

        log.debug(
                "Publishing order event: eventType={}, orderId={}, topic={}",
                event.getEventType(),
                orderId,
                ORDER_CREATED_TOPIC
        );

        return kafkaTemplate
                .send(
                        ORDER_CREATED_TOPIC,
                        orderId,
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {
                        log.error(
                                "Failed to publish order event: eventType={}, orderId={}, topic={}",
                                event.getEventType(),
                                orderId,
                                ORDER_CREATED_TOPIC,
                                throwable
                        );
                        return;
                    }

                    publishedCounter.increment();

                    log.debug(
                            "Successfully published order event: eventType={}, orderId={}, topic={}, partition={}, offset={}",
                            event.getEventType(),
                            orderId,
                            ORDER_CREATED_TOPIC,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}