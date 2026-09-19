package com.example.payment_service.service.impl;

import com.ecommerce.common.enums.PaymentStatus;
import com.ecommerce.common.kafka.EventType;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.InvalidPaymentStatusTransitionException;
import com.example.payment_service.exception.PaymentConcurrentModificationException;
import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.exception.PaymentProviderMismatchException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.metrics.PaymentMetrics;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.OutboxService;
import com.example.payment_service.service.PaymentEventFactory;
import com.example.payment_service.service.PaymentStatusLifecycle;
import com.example.payment_service.service.PaymentWebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final PaymentRepository paymentRepository;

    private final PaymentStatusLifecycle statusLifecycle;

    private final PaymentMetrics paymentMetrics;

    private final OutboxService outboxService;

    private final PaymentEventFactory paymentEventFactory;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void processRazorpayWebhook(String rawBody,
                                       String eventId) {

        paymentMetrics.webhookReceived();

        if (rawBody == null || rawBody.isBlank()) {

            paymentMetrics.webhookFailed();

            throw new IllegalArgumentException(
                    "Razorpay webhook body cannot be empty"
            );
        }

        try {

            JsonNode root = objectMapper.readTree(rawBody);

            String eventType = textValue(root, "event");

            if (eventType == null || eventType.isBlank()) {

                paymentMetrics.webhookFailed();

                throw new IllegalArgumentException(
                        "Razorpay webhook event is missing"
                );
            }

            log.info(
                    "Processing Razorpay webhook. eventId={}, eventType={}",
                    eventId,
                    eventType
            );

            switch (eventType) {

                case "payment.captured" ->
                        processPaymentEvent(
                                root,
                                eventId,
                                PaymentStatus.SUCCESS
                        );

                case "payment.failed" ->
                        processPaymentEvent(
                                root,
                                eventId,
                                PaymentStatus.FAILED
                        );

                default -> {

                    /*
                     * We intentionally acknowledge events that this
                     * service does not currently consume.
                     *
                     * Razorpay supports many webhook event types.
                     * We only need payment events for this service.
                     */
                    log.info(
                            "Ignoring unsupported Razorpay webhook event. eventId={}, eventType={}",
                            eventId,
                            eventType
                    );
                }
            }

        } catch (PaymentNotFoundException |
                 InvalidPaymentStatusTransitionException |
                 PaymentProviderMismatchException |
                 PaymentConcurrentModificationException ex) {

            paymentMetrics.webhookFailed();

            throw ex;

        } catch (Exception ex) {

            paymentMetrics.webhookFailed();

            log.error(
                    "Failed to process Razorpay webhook. eventId={}",
                    eventId,
                    ex
            );

            throw new IllegalStateException(
                    "Unable to process Razorpay webhook",
                    ex
            );
        }
    }

    private void processPaymentEvent(JsonNode root,
                                     String eventId,
                                     PaymentStatus targetStatus) {

        JsonNode paymentEntity =
                root.path("payload")
                        .path("payment")
                        .path("entity");

        if (paymentEntity.isMissingNode() || paymentEntity.isNull()) {

            throw new IllegalArgumentException(
                    "Razorpay payment entity is missing"
            );
        }

        String providerPaymentId =
                textValue(paymentEntity, "id");

        String providerOrderId =
                textValue(paymentEntity, "order_id");

        String failureReason =
                textValue(paymentEntity, "error_description");

        if (providerPaymentId == null || providerPaymentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay payment ID is missing"
            );
        }

        if (providerOrderId == null || providerOrderId.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay order ID is missing"
            );
        }

        log.info(
                "Processing Razorpay payment event. eventId={}, providerPaymentId={}, providerOrderId={}, targetStatus={}",
                eventId,
                providerPaymentId,
                providerOrderId,
                targetStatus
        );

        Payment payment =
                findPayment(
                        providerOrderId,
                        providerPaymentId
                );

        validateProvider(payment);

        validateProviderOrder(payment, providerOrderId);

        validateProviderPayment(payment, providerPaymentId);

        /*
         * Duplicate webhook/event delivery.
         *
         * If the local payment is already in the requested terminal
         * state, there is nothing else to do.
         */
        if (payment.getStatus() == targetStatus) {

            paymentMetrics.webhookDuplicate();

            log.info(
                    "Ignoring duplicate Razorpay webhook. paymentId={}, providerPaymentId={}, status={}",
                    payment.getId(),
                    providerPaymentId,
                    payment.getStatus()
            );

            return;
        }

        PaymentStatus currentStatus = payment.getStatus();

        /*
         * Razorpay webhook ordering is not guaranteed.
         *
         * For this implementation, SUCCESS is terminal for the
         * payment flow. A later FAILED webhook must never downgrade
         * a successful payment.
         */
        if (currentStatus == PaymentStatus.SUCCESS &&
                targetStatus == PaymentStatus.FAILED) {

            paymentMetrics.webhookDuplicate();

            log.warn(
                    "Ignoring FAILED webhook after SUCCESS. paymentId={}, providerPaymentId={}",
                    payment.getId(),
                    providerPaymentId
            );

            return;
        }

        if (!statusLifecycle.canTransition(
                currentStatus,
                targetStatus)) {

            paymentMetrics.webhookFailed();

            paymentMetrics.invalidStatusTransition();

            throw new InvalidPaymentStatusTransitionException(
                    currentStatus,
                    targetStatus
            );
        }

        payment.setStatus(targetStatus);

        /*
         * Razorpay payment ID is the actual payment identifier.
         */
        String existingProviderPaymentId =
                payment.getProviderPaymentId();

        if (existingProviderPaymentId != null &&
                !existingProviderPaymentId.isBlank() &&
                !existingProviderPaymentId.equals(providerPaymentId)) {

            paymentMetrics.webhookFailed();

            throw new IllegalStateException(
                    "Razorpay payment ID does not match the stored payment"
            );
        }

        payment.setProviderPaymentId(providerPaymentId);

        /*
         * providerReference represents the actual provider payment
         * once the payment has been attempted/completed.
         */
        payment.setProviderReference(providerPaymentId);

        /*
         * We already know the Razorpay order ID from the webhook.
         * Preserve it as the canonical provider order identifier.
         */
        payment.setProviderOrderId(providerOrderId);

        if (failureReason != null && !failureReason.isBlank()) {

            payment.setFailureReason(failureReason);
        }

        try {

            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            publishPaymentEvent(savedPayment, targetStatus);

            log.info(
                    "Razorpay payment processed successfully. paymentId={}, orderId={}, providerOrderId={}, providerPaymentId={}, status={}",
                    savedPayment.getId(),
                    savedPayment.getOrderId(),
                    savedPayment.getProviderOrderId(),
                    savedPayment.getProviderPaymentId(),
                    savedPayment.getStatus()
            );

        } catch (ObjectOptimisticLockingFailureException ex) {

            paymentMetrics.webhookFailed();

            paymentMetrics.concurrentModification();

            throw new PaymentConcurrentModificationException(
                    payment.getId()
            );
        }
    }

    private Payment findPayment(String providerOrderId,
                                String providerPaymentId) {

        /*
         * First locate using Razorpay's order ID because this is the
         * identifier we persisted when the Razorpay order was created.
         */
        Optional<Payment> byProviderOrderId =
                paymentRepository.findByProviderOrderId(
                        providerOrderId
                );

        if (byProviderOrderId.isPresent()) {

            return byProviderOrderId.get();
        }

        /*
         * Fallback to payment ID in case a webhook arrives after the
         * payment identifier has already been persisted but the order
         * lookup is unavailable for some reason.
         */
        return paymentRepository
                .findByProviderPaymentId(
                        providerPaymentId
                )
                .orElseThrow(() -> {

                    paymentMetrics.webhookFailed();

                    return new PaymentNotFoundException(
                            providerPaymentId
                    );
                });
    }

    private void validateProvider(Payment payment) {

        if (!"RAZORPAY".equalsIgnoreCase(
                payment.getProvider()
        )) {

            paymentMetrics.webhookFailed();

            throw new PaymentProviderMismatchException(
                    payment.getProvider(),
                    "RAZORPAY"
            );
        }
    }

    private void validateProviderOrder(Payment payment,
                                       String providerOrderId) {

        String storedProviderOrderId =
                payment.getProviderOrderId();

        if (storedProviderOrderId == null ||
                storedProviderOrderId.isBlank()) {

            payment.setProviderOrderId(providerOrderId);

            return;
        }

        if (!storedProviderOrderId.equals(providerOrderId)) {

            paymentMetrics.webhookFailed();

            throw new IllegalStateException(
                    "Razorpay order ID does not match the stored payment"
            );
        }
    }

    private void validateProviderPayment(Payment payment,
                                         String providerPaymentId) {

        String storedProviderPaymentId =
                payment.getProviderPaymentId();

        if (storedProviderPaymentId == null ||
                storedProviderPaymentId.isBlank()) {

            return;
        }

        if (!storedProviderPaymentId.equals(providerPaymentId)) {

            paymentMetrics.webhookFailed();

            throw new IllegalStateException(
                    "Razorpay payment ID does not match the stored payment"
            );
        }
    }

    private void publishPaymentEvent(Payment payment,
                                     PaymentStatus targetStatus) {

        if (targetStatus == PaymentStatus.SUCCESS) {

            outboxService.savePaymentCompletedEvent(
                    paymentEventFactory.createPaymentEvent(
                            EventType.PAYMENT_SUCCESSFUL,
                            payment
                    )
            );

        } else if (targetStatus == PaymentStatus.FAILED) {

            outboxService.savePaymentCompletedEvent(
                    paymentEventFactory.createPaymentEvent(
                            EventType.PAYMENT_FAILED,
                            payment
                    )
            );
        }
    }

    private String textValue(JsonNode node, String field) {

        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {

            return null;
        }

        return value.asText();
    }
}