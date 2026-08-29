package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.*;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.metrics.PaymentMetrics;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentStatusLifecycle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPersistenceService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final PaymentStatusLifecycle statusLifecycle;

    private final PaymentMetrics paymentMetrics;

    @Transactional
    public PaymentCreationResult createPendingPayment(OrderResponse order,
                                                      String providerName) {

        Payment existingPayment =
                paymentRepository
                        .findByOrderId(order.getId())
                        .orElse(null);

        if (existingPayment != null) {

            paymentMetrics.duplicatePayment();

            log.info(
                    "Payment already exists for order {}. " +
                            "paymentId={}, status={}",
                    order.getId(),
                    existingPayment.getId(),
                    existingPayment.getStatus()
            );

            return new PaymentCreationResult(
                    paymentMapper.toResponse(existingPayment),
                    false
            );
        }

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentMethod(order.getPaymentMethod())
                .provider(providerName)
                .status(PaymentStatus.PENDING)
                .build();

        try {

            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            paymentMetrics.paymentCreated();

            log.info(
                    "Created new payment {} for order {}",
                    savedPayment.getId(),
                    order.getId()
            );

            return new PaymentCreationResult(
                    paymentMapper.toResponse(savedPayment),
                    true
            );

        } catch (DataIntegrityViolationException ex) {

            /*
             * Race condition:
             *
             * Thread A -> inserts payment
             * Thread B -> INSERT fails because of UNIQUE(order_id)
             *
             * Therefore try to retrieve the payment created by
             * the other transaction.
             */
            Payment concurrentPayment =
                    paymentRepository
                            .findByOrderId(order.getId())
                            .orElse(null);

            if (concurrentPayment != null) {

                paymentMetrics.duplicatePayment();

                log.info(
                        "Payment was created concurrently for order {}. " +
                                "Returning payment {}.",
                        order.getId(),
                        concurrentPayment.getId()
                );

                return new PaymentCreationResult(
                        paymentMapper.toResponse(concurrentPayment),
                        false
                );
            }

            log.error(
                    "Payment creation failed for order {} and no " +
                            "existing payment could be found.",
                    order.getId(),
                    ex
            );

            throw new PaymentConcurrencyException(
                    order.getId()
            );
        }
    }

    @Transactional
    public PaymentResponse markPaymentProcessing(Long paymentId,
                                                 PaymentProviderResponse providerResponse) {

        if (providerResponse == null) {

            throw new InvalidPaymentProviderResponseException(
                    paymentId
            );
        }

        if (providerResponse.status() == null) {

            throw new InvalidPaymentProviderResponseException(
                    paymentId
            );
        }

        if (providerResponse.providerReference() == null ||
                providerResponse.providerReference().isBlank()) {

            throw new InvalidPaymentProviderResponseException(
                    paymentId
            );
        }

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(paymentId)
                        );

        /*
         * --------------------------------------------------
         * PROVIDER REFERENCE IDEMPOTENCY
         * --------------------------------------------------
         */

        if (payment.getProviderReference() != null &&
                !payment.getProviderReference()
                        .equals(providerResponse.providerReference())) {

            throw new PaymentProviderReferenceMismatchException(
                    paymentId,
                    payment.getProviderReference(),
                    providerResponse.providerReference()
            );
        }

        if (payment.getStatus() == providerResponse.status()) {

            /*
             * Same status + same provider reference.
             */
            log.info(
                    "Ignoring duplicate provider response: " +
                            "paymentId={}, status={}, providerReference={}",
                    paymentId,
                    providerResponse.status(),
                    providerResponse.providerReference()
            );

            return paymentMapper.toResponse(payment);
        }

        transitionStatus(
                payment,
                providerResponse.status()
        );

        payment.setProviderReference(providerResponse.providerReference());

        try {

            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            return paymentMapper.toResponse(savedPayment);

        } catch (ObjectOptimisticLockingFailureException ex) {

            paymentMetrics.concurrentModification();

            log.warn(
                    "Concurrent modification while updating payment {}",
                    paymentId
            );

            throw new PaymentConcurrencyException(
                    paymentId
            );
        }
    }

    private void transitionStatus(Payment payment,
                                  PaymentStatus targetStatus) {

        PaymentStatus currentStatus = payment.getStatus();

        if (currentStatus == targetStatus) {
            return;
        }

        if (!statusLifecycle.canTransition(
                currentStatus,
                targetStatus)) {

            paymentMetrics.invalidStatusTransition();

            throw new InvalidPaymentStatusTransitionException(
                    currentStatus,
                    targetStatus
            );
        }

        payment.setStatus(targetStatus);

        if (targetStatus == PaymentStatus.SUCCESS) {

            paymentMetrics.paymentSucceeded();

        } else if (targetStatus == PaymentStatus.FAILED) {

            paymentMetrics.paymentFailed();

        } else if (targetStatus == PaymentStatus.CANCELLED) {

            paymentMetrics.paymentCancelled();
        }
    }

    public record PaymentCreationResult(
            PaymentResponse payment,
            boolean created
    ) {
    }
}