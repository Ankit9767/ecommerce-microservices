package com.example.payment_service.kafka;

import com.ecommerce.common.events.PaymentEvent;
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
public class PaymentCompletedProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private final MeterRegistry meterRegistry;

    public void publish(PaymentEvent event) {

        log.info("Publishing {} on topic {} : {}", event.getEventType(),
                KafkaTopics.PAYMENT_COMPLETED, event);

        kafkaTemplate.send(
                KafkaTopics.PAYMENT_COMPLETED,
                event.getOrderId().toString(),
                event
        );

        Counter.builder("kafka.payment.events.published")
                .description("Payment events published to Kafka")
                .register(meterRegistry)
                .increment();
    }
}