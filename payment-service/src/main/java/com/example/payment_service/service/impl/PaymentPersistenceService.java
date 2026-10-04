package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.OrderStatus;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPersistenceService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final PaymentStatusLifecycle statusLifecycle;

    private final PaymentMetrics paymentMetrics;

    private static final List<PaymentStatus> ACTIVE_STATUSES =
            List.of(
                    PaymentStatus.PENDING,
                    PaymentStatus.PROCESSING
            );

    @Transactional
    public PaymentCreationResult createPendingPayment(OrderResponse order,
                                                      String providerName) {

        Payment existingPayment =
                paymentRepository
                        .findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
                                order.getId(),
                                ACTIVE_STATUSES
                        )
                        .orElse(null);

        if (existingPayment != null) {

            paymentMetrics.duplicatePayment();

            log.info(
                    "Active payment already exists for order {}. " +
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
                    "Created new payment {} for order {}. provider={}",
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
             * The UNIQUE constraint on active_order_id protects
             * us against concurrent creation of an active payment.
             *
             * Another request may have inserted the active payment
             * after our initial lookup.
             */

            Payment concurrentPayment =
                    paymentRepository
                            .findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
                                    order.getId(),
                                    ACTIVE_STATUSES
                            )
                            .orElse(null);

            if (concurrentPayment != null) {

                paymentMetrics.duplicatePayment();

                log.info(
                        "Active payment was created concurrently for order {}. " +
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
                    "Payment creation failed for order {} " +
                            "without an existing active payment.",
                    order.getId(),
                    ex
            );

            throw ex;
        }
    }

    @Transactional
    public PaymentCreationResult createRetryPayment(OrderResponse order,
                                                    String providerName) {

        if (order == null || order.getId() == null) {

            throw new IllegalArgumentException(
                    "Order is required to create a retry payment"
            );
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {

            throw new IllegalStateException(
                    "Payment can only be retried while the order is awaiting payment"
            );
        }

        if (paymentRepository.existsByOrderIdAndStatus(
                order.getId(),
                PaymentStatus.SUCCESS
        )) {

            log.warn(
                    "Retry rejected because order {} already has a successful payment",
                    order.getId()
            );

            throw new IllegalStateException("Order has already been paid");
        }

        /*
         * If another retry request already created an active
         * attempt, return it instead of creating another one.
         */
        Payment existingActivePayment =
                paymentRepository
                        .findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
                                order.getId(),
                                ACTIVE_STATUSES
                        )
                        .orElse(null);

        if (existingActivePayment != null) {

            log.info(
                    "Active payment already exists for retry. " +
                            "orderId={}, paymentId={}, status={}",
                    order.getId(),
                    existingActivePayment.getId(),
                    existingActivePayment.getStatus()
            );

            return new PaymentCreationResult(
                    paymentMapper.toResponse(existingActivePayment),
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

            Payment savedPayment =
                    paymentRepository.saveAndFlush(payment);

            paymentMetrics.paymentCreated();

            log.info(
                    "Created payment retry attempt {} for order {}. provider={}",
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
             * Another request may have won the race and created
             * the active payment first.
             */

            Payment concurrentPayment =
                    paymentRepository
                            .findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
                                    order.getId(),
                                    ACTIVE_STATUSES
                            )
                            .orElse(null);

            if (concurrentPayment != null) {

                log.info(
                        "Retry payment was created concurrently for order {}. " +
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
                    "Failed to create retry payment for order {}",
                    order.getId(),
                    ex
            );

            throw ex;
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