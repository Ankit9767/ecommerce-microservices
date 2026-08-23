package com.example.payment_service.kafka;

import com.ecommerce.common.events.PaymentEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class PaymentCompletedProducer {

    private static final String PAYMENT_COMPLETED_TOPIC =
            KafkaTopics.PAYMENT_COMPLETED;

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private final Counter publishedCounter;

    private final Counter failedCounter;

    public PaymentCompletedProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate,
                                    MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter = Counter.builder("kafka.payment.events.published")
                .description("Number of payment events successfully published to Kafka")
                .tag("topic", PAYMENT_COMPLETED_TOPIC)
                .register(meterRegistry);

        this.failedCounter = Counter.builder("kafka.payment.events.failed")
                .description("Number of payment events that failed to publish to Kafka")
                .tag("topic", PAYMENT_COMPLETED_TOPIC)
                .register(meterRegistry);
    }

    public CompletableFuture<?> publish(PaymentEvent event) {

        String orderId = event.getOrderId().toString();

        log.debug(
                "Publishing payment event: orderId={}, topic={}",
                orderId,
                PAYMENT_COMPLETED_TOPIC
        );

        return kafkaTemplate
                .send(
                        PAYMENT_COMPLETED_TOPIC,
                        orderId,
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {
                        failedCounter.increment();

                        log.error(
                                "Failed to publish payment event: orderId={}, topic={}",
                                orderId,
                                PAYMENT_COMPLETED_TOPIC,
                                throwable
                        );

                        return;
                    }

                    publishedCounter.increment();

                    log.debug(
                            "Successfully published payment event: orderId={}, topic={}, partition={}, offset={}",
                            orderId,
                            PAYMENT_COMPLETED_TOPIC,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}