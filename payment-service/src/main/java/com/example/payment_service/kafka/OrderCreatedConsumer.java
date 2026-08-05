package com.example.payment_service.kafka;

import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.payment_service.Service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "payment-group"
    )
    public void consume(OrderCreatedEvent event) throws JsonProcessingException {

        log.info("Received Order Event : {}", event);

        paymentService.processOrderEvent(event);

    }

    @DltHandler
    public void handleDeadLetter(OrderCreatedEvent event) {

        log.error("Received DLT Event {}", event);

    }

}
