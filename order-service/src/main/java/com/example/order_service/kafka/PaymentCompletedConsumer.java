package com.example.order_service.kafka;

import com.ecommerce.common.events.PaymentEvent;
import com.ecommerce.common.exception.InvalidEventException;
import com.ecommerce.common.exception.MissingEventIdException;
import com.ecommerce.common.exception.MissingEventTypeException;
import com.ecommerce.common.kafka.EventIdempotencyService;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.order_service.service.OrderService;
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
public class PaymentCompletedConsumer {

    private final OrderService orderService;

    private final EventIdempotencyService eventIdempotencyService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "order-group"
    )
    public void consume(PaymentEvent event) {

        validateEvent(event);

        log.info(
                "Received PaymentEvent: eventId={}, eventType={}, orderId={}",
                event.getEventId(),
                event.getEventType(),
                event.getOrderId()
        );

        EventType eventType = event.getEventType();

        if (eventType != EventType.PAYMENT_SUCCESSFUL &&
                eventType != EventType.PAYMENT_FAILED) {

            log.warn(
                    "Ignoring unexpected payment event type: " +
                            "eventId={}, eventType={}, orderId={}",
                    event.getEventId(),
                    eventType,
                    event.getOrderId()
            );

            return;
        }

        if (eventIdempotencyService.alreadyProcessed(event)) {

            log.info(
                    "Ignoring already processed PaymentEvent: eventId={}, " +
                            "eventType={}, orderId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getOrderId()
            );

            return;
        }

        switch (eventType) {

            case PAYMENT_SUCCESSFUL -> {

                orderService.handlePaymentCompleted(event);

                markProcessed(event);
            }

            case PAYMENT_FAILED -> {

                orderService.handlePaymentFailed(event);

                markProcessed(event);
            }

            default -> {

                log.warn(
                        "Ignoring unexpected payment event type: eventId={}, " +
                                "eventType={}, orderId={}",
                        event.getEventId(),
                        eventType,
                        event.getOrderId()
                );
            }
        }
    }

    private void markProcessed(PaymentEvent event) {

        boolean marked = eventIdempotencyService.markProcessed(event);

        if (!marked) {

            log.info(
                    "Payment event was processed concurrently: " +
                            "eventId={}, eventType={}, orderId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getOrderId()
            );
        }
    }

    private void validateEvent(PaymentEvent event) {

        if (event == null) {
            throw new InvalidEventException();
        }

        if (event.getEventId() == null) {
            throw new MissingEventIdException();
        }

        if (event.getEventType() == null) {
            throw new MissingEventTypeException();
        }
    }

    @DltHandler
    public void handleDeadLetter(PaymentEvent event) {

        log.error(
                "Payment event moved to DLT after retries exhausted: " +
                        "eventId={}, eventType={}, orderId={}, event={}",
                event != null ? event.getEventId() : null,
                event != null ? event.getEventType() : null,
                event != null ? event.getOrderId() : null,
                event
        );
    }
}