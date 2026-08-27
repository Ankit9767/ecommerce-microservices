package com.example.payment_service.kafka;

import com.ecommerce.common.events.OrderCancelledEvent;
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
public class OrderCancelledConsumer {

    private final PaymentService paymentService;

    private final EventIdempotencyService eventIdempotencyService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            groupId = "payment-cancellation-group"
    )
    public void consume(OrderCancelledEvent event) {

        validateEvent(event);

        log.info(
                "Received ORDER_CANCELLED event: " +
                        "eventId={}, orderId={}, reason={}",
                event.getEventId(),
                event.getOrderId(),
                event.getReason()
        );

        if (event.getEventType() != EventType.ORDER_CANCELLED) {

            log.debug(
                    "Ignoring unexpected event type: eventId={}, " +
                            "eventType={}, orderId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getOrderId()
            );

            return;
        }

        if (eventIdempotencyService.alreadyProcessed(event)) {

            log.info(
                    "Ignoring already processed ORDER_CANCELLED event: " +
                            "eventId={}, orderId={}",
                    event.getEventId(),
                    event.getOrderId()
            );

            return;
        }

        paymentService.processOrderCancelledEvent(event);

        markProcessed(event);
    }

    private void markProcessed(OrderCancelledEvent event) {

        boolean marked = eventIdempotencyService.markProcessed(event);

        if (!marked) {

            log.info(
                    "ORDER_CANCELLED event was processed concurrently: " +
                            "eventId={}, orderId={}",
                    event.getEventId(),
                    event.getOrderId()
            );
        }
    }

    private void validateEvent(OrderCancelledEvent event) {

        if (event == null) {

            throw new InvalidEventIdException(
                    "OrderCancelledEvent must not be null"
            );
        }

        if (event.getEventId() == null) {

            throw new InvalidEventIdException(
                    "OrderCancelledEvent must contain an eventId"
            );
        }

        if (event.getEventType() == null) {

            throw new InvalidEventIdException(
                    "OrderCancelledEvent must contain an eventType"
            );
        }
    }

    @DltHandler
    public void handleDeadLetter(OrderCancelledEvent event) {

        log.error(
                "Order-cancelled event moved to DLT after retries exhausted: " +
                        "eventId={}, eventType={}, orderId={}, event={}",
                event != null ? event.getEventId() : null,
                event != null ? event.getEventType() : null,
                event != null ? event.getOrderId() : null,
                event
        );
    }
}