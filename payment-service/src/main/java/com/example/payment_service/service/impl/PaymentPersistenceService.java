package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.InvalidPaymentProviderResponseException;
import com.example.payment_service.exception.InvalidPaymentStatusTransitionException;
import com.example.payment_service.exception.PaymentConcurrencyException;
import com.example.payment_service.exception.PaymentNotFoundException;
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
                            "paymentId={}, status={}, providerOrderId={}, " +
                            "providerPaymentId={}",
                    order.getId(),
                    existingPayment.getId(),
                    existingPayment.getStatus(),
                    existingPayment.getProviderOrderId(),
                    existingPayment.getProviderPaymentId()
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
                    "Created new payment {} for order {}. " +
                            "provider={}",
                    savedPayment.getId(),
                    order.getId(),
                    providerName
            );

            return new PaymentCreationResult(
                    paymentMapper.toResponse(savedPayment),
                    true
            );

        } catch (DataIntegrityViolationException ex) {

            /*
             * --------------------------------------------------
             * CONCURRENT PAYMENT CREATION
             *
             * UNIQUE(order_id) prevents two payments for
             * the same order.
             * --------------------------------------------------
             */

            Payment concurrentPayment =
                    paymentRepository
                            .findByOrderId(order.getId())
                            .orElse(null);

            if (concurrentPayment != null) {

                paymentMetrics.duplicatePayment();

                log.info(
                        "Payment was created concurrently for order {}. " +
                                "Returning payment {}. " +
                                "status={}, providerOrderId={}",
                        order.getId(),
                        concurrentPayment.getId(),
                        concurrentPayment.getStatus(),
                        concurrentPayment.getProviderOrderId()
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

        validateProviderResponse(paymentId, providerResponse);

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        paymentId
                                )
                        );

        /*
         * --------------------------------------------------
         * PROVIDER ORDER ID
         * --------------------------------------------------
         *
         * The provider order ID is created once.
         *
         * Example:
         *
         * order_ABC123
         *
         * Once stored, a different provider order ID must
         * never silently overwrite it.
         * --------------------------------------------------
         */

        if (providerResponse.providerOrderId() != null) {

            String existingProviderOrderId =
                    payment.getProviderOrderId();

            if (existingProviderOrderId != null &&
                    !existingProviderOrderId.isBlank() &&
                    !existingProviderOrderId.equals(
                            providerResponse.providerOrderId()
                    )) {

                log.error(
                        "Provider order ID mismatch for payment {}. " +
                                "existing={}, incoming={}",
                        paymentId,
                        existingProviderOrderId,
                        providerResponse.providerOrderId()
                );

                throw new InvalidPaymentProviderResponseException(
                        paymentId
                );
            }

            payment.setProviderOrderId(
                    providerResponse.providerOrderId()
            );
        }

        /*
         * --------------------------------------------------
         * PROVIDER PAYMENT ID
         * --------------------------------------------------
         *
         * Created later when the customer actually pays.
         *
         * Example:
         *
         * pay_XYZ123
         * --------------------------------------------------
         */

        if (providerResponse.providerPaymentId() != null) {

            String existingProviderPaymentId =
                    payment.getProviderPaymentId();

            if (existingProviderPaymentId != null &&
                    !existingProviderPaymentId.isBlank() &&
                    !existingProviderPaymentId.equals(
                            providerResponse.providerPaymentId()
                    )) {

                log.error(
                        "Provider payment ID mismatch for payment {}. " +
                                "existing={}, incoming={}",
                        paymentId,
                        existingProviderPaymentId,
                        providerResponse.providerPaymentId()
                );

                throw new InvalidPaymentProviderResponseException(
                        paymentId
                );
            }

            payment.setProviderPaymentId(
                    providerResponse.providerPaymentId()
            );
        }

        /*
         * --------------------------------------------------
         * PROVIDER REFERENCE
         * --------------------------------------------------
         *
         * Initial state:
         *
         * order_ABC123
         *
         * After actual payment:
         *
         * pay_XYZ123
         *
         * Therefore changing the provider reference is
         * intentional.
         * --------------------------------------------------
         */

        if (payment.getProviderReference() != null &&
                !payment.getProviderReference()
                        .equals(providerResponse.providerReference())) {

            log.info(
                    "Provider reference changed for payment {}. " +
                            "oldReference={}, newReference={}",
                    paymentId,
                    payment.getProviderReference(),
                    providerResponse.providerReference()
            );
        }

        payment.setProviderReference(
                providerResponse.providerReference()
        );

        /*
         * --------------------------------------------------
         * STATUS
         * --------------------------------------------------
         */

        if (payment.getStatus() == providerResponse.status()) {

            if (providerResponse.failureReason() != null) {

                payment.setFailureReason(
                        providerResponse.failureReason()
                );
            }

            try {

                Payment savedPayment =
                        paymentRepository.saveAndFlush(
                                payment
                        );

                return paymentMapper.toResponse(savedPayment);

            } catch (
                    ObjectOptimisticLockingFailureException ex
            ) {

                paymentMetrics.concurrentModification();

                log.warn(
                        "Concurrent modification while updating " +
                                "provider information for payment {}",
                        paymentId
                );

                throw new PaymentConcurrencyException(
                        paymentId
                );
            }
        }

        /*
         * --------------------------------------------------
         * STATUS TRANSITION
         * --------------------------------------------------
         */

        transitionStatus(payment, providerResponse.status());

        payment.setFailureReason(
                providerResponse.failureReason()
        );

        try {

            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            log.info(
                    "Updated payment {}. status={}, " +
                            "providerOrderId={}, providerPaymentId={}",
                    savedPayment.getId(),
                    savedPayment.getStatus(),
                    savedPayment.getProviderOrderId(),
                    savedPayment.getProviderPaymentId()
            );

            return paymentMapper.toResponse(savedPayment);

        } catch (
                ObjectOptimisticLockingFailureException ex
        ) {

            paymentMetrics.concurrentModification();

            log.warn(
                    "Concurrent modification while updating payment {}",
                    paymentId
            );

            throw new PaymentConcurrencyException(paymentId);
        }
    }

    private void validateProviderResponse(Long paymentId,
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

        /*
         * For a provider-created order we need the provider
         * order ID.
         *
         * For future webhook responses this validation can
         * be handled according to the event type because a
         * webhook may contain a payment ID as the primary
         * identifier.
         */
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