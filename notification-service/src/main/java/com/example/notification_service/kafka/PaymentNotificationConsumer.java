package com.example.notification_service.kafka;

import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.exception.*;
import com.ecommerce.common.kafka.EventIdempotencyService;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.notification_service.metrics.NotificationMetrics;
import com.example.notification_service.service.impl.NotificationService;
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
public class PaymentNotificationConsumer {

    private final NotificationService notificationService;

    private final EventIdempotencyService eventIdempotencyService;

    private final NotificationMetrics notificationMetrics;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "notification-payment-group"
    )
    public void consume(PaymentCompletedEvent event) {

        validateEvent(event);

        log.info(
                "Received payment event: " +
                        "eventId={}, eventType={}, orderId={}, paymentId={}",
                event.getEventId(),
                event.getEventType(),
                event.getOrderId(),
                event.getPaymentId()
        );

        if (eventIdempotencyService.alreadyProcessed(event)) {

            notificationMetrics.duplicateEvent();

            log.info(
                    "Ignoring already processed payment event: " +
                            "eventId={}",
                    event.getEventId()
            );

            return;
        }

        switch (event.getEventType()) {

            case PAYMENT_SUCCESSFUL -> {

                notificationService.handlePaymentSuccessful(event);

                eventIdempotencyService.markProcessed(event);
            }

            case PAYMENT_FAILED -> {

                notificationService.handlePaymentFailed(event);

                eventIdempotencyService.markProcessed(event);
            }

            default -> log.warn(
                    "Ignoring unsupported payment event type: " +
                            "eventType={}, eventId={}",
                    event.getEventType(),
                    event.getEventId()
            );
        }
    }

    private void validateEvent(PaymentCompletedEvent event) {

        if (event == null) {
            throw new InvalidEventException();
        }

        if (event.getEventId() == null) {
            throw new MissingEventIdException();
        }

        if (event.getEventType() == null) {
            throw new MissingEventTypeException();
        }

        if (event.getPaymentId() == null) {
            throw new MissingPaymentIdException();
        }

        if (event.getOrderId() == null) {
            throw new MissingOrderIdException();
        }
    }


    @DltHandler
    public void handleDeadLetter(PaymentCompletedEvent event) {

        log.error(
                "Payment notification event moved to DLT: " +
                        "eventId={}, eventType={}, orderId={}, paymentId={}",
                event != null
                        ? event.getEventId()
                        : null,
                event != null
                        ? event.getEventType()
                        : null,
                event != null
                        ? event.getOrderId()
                        : null,
                event != null
                        ? event.getPaymentId()
                        : null
        );
    }
}