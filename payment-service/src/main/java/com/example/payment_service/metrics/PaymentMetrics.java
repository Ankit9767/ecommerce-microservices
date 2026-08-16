package com.example.payment_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {

    private final Counter paymentsCreated;

    private final Counter paymentsViewed;

    private final Counter paymentsNotFound;

    private final Counter duplicatePayments;

    private final Counter paymentsCancelled;

    private final Counter paymentsSucceeded;

    private final Counter paymentsFailed;

    private final Counter invalidStatusTransition;

    private final Counter orderAccessDenied;

    private final Counter webhooksReceived;

    private final Counter webhookFailures;

    private final Counter concurrentModifications;

    public PaymentMetrics(MeterRegistry meterRegistry) {

        paymentsCreated =
                Counter.builder("payment.created")
                        .description(
                                "Number of payments created"
                        )
                        .register(meterRegistry);

        paymentsViewed =
                Counter.builder("payment.viewed")
                        .description(
                                "Number of payment lookups"
                        )
                        .register(meterRegistry);

        paymentsNotFound =
                Counter.builder("payment.not_found")
                        .description(
                                "Number of payment not found events"
                        )
                        .register(meterRegistry);

        duplicatePayments =
                Counter.builder("payment.duplicate")
                        .description(
                                "Number of duplicate payment attempts"
                        )
                        .register(meterRegistry);

        paymentsCancelled =
                Counter.builder("payment.cancelled")
                        .description(
                                "Number of payments cancelled"
                        )
                        .register(meterRegistry);

        paymentsSucceeded =
                Counter.builder("payment.succeeded")
                        .description(
                                "Number of successful payments"
                        )
                        .register(meterRegistry);

        paymentsFailed =
                Counter.builder("payment.failed")
                        .description(
                                "Number of failed payments"
                        )
                        .register(meterRegistry);

        invalidStatusTransition =
                Counter.builder(
                                "payment.invalid_status_transition"
                        )
                        .description(
                                "Number of invalid payment status transition attempts"
                        )
                        .register(meterRegistry);

        orderAccessDenied =
                Counter.builder(
                                "payment.order_access_denied"
                        )
                        .description(
                                "Number of payment attempts denied because the customer does not own the order"
                        )
                        .register(meterRegistry);

        webhooksReceived =
                Counter.builder(
                                "payment.webhook.received"
                        )
                        .description(
                                "Number of payment webhooks received"
                        )
                        .register(meterRegistry);

        webhookFailures =
                Counter.builder(
                                "payment.webhook.failed"
                        )
                        .description(
                                "Number of failed payment webhook events"
                        )
                        .register(meterRegistry);

        concurrentModifications =
                Counter.builder(
                                "payment.concurrent_modification"
                        )
                        .description(
                                "Number of concurrent payment modification conflicts"
                        )
                        .register(meterRegistry);
    }

    public void paymentCreated() {
        paymentsCreated.increment();
    }

    public void paymentViewed() {
        paymentsViewed.increment();
    }

    public void paymentNotFound() {
        paymentsNotFound.increment();
    }

    public void duplicatePayment() {
        duplicatePayments.increment();
    }

    public void paymentCancelled() {
        paymentsCancelled.increment();
    }

    public void paymentSucceeded() {
        paymentsSucceeded.increment();
    }

    public void paymentFailed() {
        paymentsFailed.increment();
    }

    public void invalidStatusTransition() {
        invalidStatusTransition.increment();
    }

    public void orderAccessDenied() {
        orderAccessDenied.increment();
    }

    public void webhookReceived() {
        webhooksReceived.increment();
    }

    public void webhookFailed() {
        webhookFailures.increment();
    }

    public void concurrentModification() {
        concurrentModifications.increment();
    }
}