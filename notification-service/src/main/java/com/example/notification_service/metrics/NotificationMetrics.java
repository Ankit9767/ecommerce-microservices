package com.example.notification_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {

    private final Counter notificationsCreated;

    private final Counter notificationsSent;

    private final Counter notificationsFailed;

    private final Counter duplicateEvents;

    private final Counter processingErrors;

    public NotificationMetrics(MeterRegistry meterRegistry) {

        notificationsCreated = Counter.builder("notification.created")
                .description("Number of notifications created")
                .register(meterRegistry);

        notificationsSent = Counter.builder("notification.sent")
                .description("Number of notifications sent")
                .register(meterRegistry);

        notificationsFailed = Counter.builder("notification.failed")
                .description("Number of notifications failed")
                .register(meterRegistry);

        duplicateEvents = Counter.builder("notification.duplicate_events")
                .description("Number of duplicate notification events")
                .register(meterRegistry);

        processingErrors = Counter.builder("notification.processing_errors")
                .description("Number of notification processing errors")
                .register(meterRegistry);
    }

    public void notificationCreated() {
        notificationsCreated.increment();
    }

    public void notificationSent() {
        notificationsSent.increment();
    }

    public void notificationFailed() {
        notificationsFailed.increment();
    }

    public void duplicateEvent() {
        duplicateEvents.increment();
    }

    public void processingError() {
        processingErrors.increment();
    }
}
