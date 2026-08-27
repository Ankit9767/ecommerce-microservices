package com.example.payment_service.kafka;

import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.exception.InvalidEventIdException;
import com.ecommerce.common.kafka.EventIdempotencyService;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.payment_service.service.PaymentService;
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

    private final EventIdempotencyService eventIdempotencyService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "payment-group"
    )
    public void consume(OrderEvent event) {

        validateEvent(event);

        log.info(
                "Received OrderEvent: eventId={}, eventType={}, orderId={}",
                event.getEventId(),
                event.getEventType(),
                event.getOrderId()
        );

        if (event.getEventType() != EventType.ORDER_CREATED) {

            log.debug(
                    "Ignoring OrderEvent type {} for payment processing: " +
                            "eventId={}, orderId={}",
                    event.getEventType(),
                    event.getEventId(),
                    event.getOrderId()
            );

            return;
        }

        if (eventIdempotencyService.alreadyProcessed(event)) {

            log.info(
                    "Ignoring already processed ORDER_CREATED event: " +
                            "eventId={}, orderId={}",
                    event.getEventId(),
                    event.getOrderId()
            );

            return;
        }

        paymentService.processOrderCreatedEvent(event);

        markProcessed(event);
    }

    private void markProcessed(OrderEvent event) {

        boolean marked = eventIdempotencyService.markProcessed(event);

        if (!marked) {

            log.info(
                    "ORDER_CREATED event was processed concurrently: " +
                            "eventId={}, orderId={}",
                    event.getEventId(),
                    event.getOrderId()
            );
        }
    }

    private void validateEvent(OrderEvent event) {

        if (event == null) {

            throw new InvalidEventIdException(
                    "OrderEvent must not be null"
            );
        }

        if (event.getEventId() == null) {

            throw new InvalidEventIdException(
                    "OrderEvent must contain an eventId"
            );
        }

        if (event.getEventType() == null) {

            throw new InvalidEventIdException(
                    "OrderEvent must contain an eventType"
            );
        }
    }

    @DltHandler
    public void handleDeadLetter(OrderEvent event) {

        log.error(
                "Order-created event moved to DLT after retries exhausted: " +
                        "eventId={}, eventType={}, orderId={}, event={}",
                event != null ? event.getEventId() : null,
                event != null ? event.getEventType() : null,
                event != null ? event.getOrderId() : null,
                event
        );
    }
}