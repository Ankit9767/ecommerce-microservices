package com.example.shipping_service.kafka;

import com.ecommerce.common.events.ShipmentEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ShipmentEventProducer {

    private static final String SHIPMENT_EVENTS_TOPIC =
            KafkaTopics.SHIPMENT_EVENTS;

    private final KafkaTemplate<String, ShipmentEvent> kafkaTemplate;

    private final Counter publishedCounter;

    private final Counter failedCounter;

    public ShipmentEventProducer(KafkaTemplate<String, ShipmentEvent> kafkaTemplate,
                                 MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter = Counter.builder(
                        "kafka.shipment.events.published"
                )
                .description(
                        "Number of shipment events successfully published to Kafka"
                )
                .tag("topic", SHIPMENT_EVENTS_TOPIC)
                .register(meterRegistry);

        this.failedCounter = Counter.builder(
                        "kafka.shipment.events.failed"
                )
                .description(
                        "Number of shipment events that failed to publish to Kafka"
                )
                .tag("topic", SHIPMENT_EVENTS_TOPIC)
                .register(meterRegistry);
    }

    public CompletableFuture<?> publish(ShipmentEvent event) {

        String shipmentId = event.getShipmentId().toString();

        log.debug(
                "Publishing shipment event: shipmentId={}, eventType={}, topic={}",
                shipmentId,
                event.getEventType(),
                SHIPMENT_EVENTS_TOPIC
        );

        return kafkaTemplate
                .send(
                        SHIPMENT_EVENTS_TOPIC,
                        shipmentId,
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {

                        failedCounter.increment();

                        log.error(
                                "Failed to publish shipment event: " +
                                        "shipmentId={}, eventType={}, topic={}",
                                shipmentId,
                                event.getEventType(),
                                SHIPMENT_EVENTS_TOPIC,
                                throwable
                        );

                        return;
                    }

                    publishedCounter.increment();

                    log.debug(
                            "Successfully published shipment event: " +
                                    "shipmentId={}, eventType={}, topic={}, " +
                                    "partition={}, offset={}",
                            shipmentId,
                            event.getEventType(),
                            SHIPMENT_EVENTS_TOPIC,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}
