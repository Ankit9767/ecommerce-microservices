package com.example.payment_service.kafka;

import com.ecommerce.common.events.PaymentCompletedEvent;
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

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    private final MeterRegistry meterRegistry;

    public void publish(PaymentCompletedEvent event) {

        log.info("Publishing PaymentCompletedEvent : {}", event);

        kafkaTemplate.send(
                KafkaTopics.PAYMENT_COMPLETED,
                event.getOrderId().toString(),
                event
        );

        Counter.builder("outbox.events.published")
                .description("Events successfully published from Outbox")
                .register(meterRegistry)
                .increment();
    }
}