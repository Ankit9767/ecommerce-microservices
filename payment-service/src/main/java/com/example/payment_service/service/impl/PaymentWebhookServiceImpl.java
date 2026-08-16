package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.dto.webhook.PaymentWebhookRequest;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.InvalidPaymentStatusTransitionException;
import com.example.payment_service.exception.PaymentConcurrentModificationException;
import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.exception.PaymentProviderMismatchException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.metrics.PaymentMetrics;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentStatusLifecycle;
import com.example.payment_service.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final PaymentStatusLifecycle statusLifecycle;

    private final PaymentMetrics paymentMetrics;

    @Override
    @Transactional
    public PaymentResponse processWebhook(PaymentWebhookRequest request) {

        paymentMetrics.webhookReceived();

        Payment payment =
                paymentRepository
                        .findByProviderReference(
                                request.providerReference()
                        )
                        .orElseThrow(() -> {
                            paymentMetrics.webhookFailed();

                            return new PaymentNotFoundException(
                                    request.providerReference()
                            );
                        });

        /*
         * Make sure the webhook belongs to
         * the provider associated with this payment.
         */
        if (!payment.getProvider()
                .equalsIgnoreCase(request.provider())) {

            paymentMetrics.webhookFailed();
            throw new PaymentProviderMismatchException(
                    payment.getProvider(),
                    request.provider()
            );
        }

        /*
         * Ignore duplicate webhook events
         * that report the current state.
         */
        if (payment.getStatus() == request.status()) {
            return paymentMapper.toResponse(payment);
        }

        PaymentStatus currentStatus = payment.getStatus();

        PaymentStatus targetStatus = request.status();

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

        if (request.failureReason() != null) {
            payment.setFailureReason(request.failureReason());
        }

        try {

            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            return paymentMapper.toResponse(savedPayment);

        } catch (ObjectOptimisticLockingFailureException ex) {

            paymentMetrics.webhookFailed();
            paymentMetrics.concurrentModification();
            throw new PaymentConcurrentModificationException(
                    payment.getId()
            );
        }
    }
}