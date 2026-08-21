package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.kafka.EventType;
import com.example.payment_service.dto.CreatePaymentRequest;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.DuplicatePaymentException;
import com.example.payment_service.exception.InvalidPaymentStatusTransitionException;
import com.example.payment_service.exception.PaymentConcurrencyException;
import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.metrics.PaymentMetrics;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.OutboxService;
import com.example.payment_service.service.PaymentStatusLifecycle;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentPersistenceService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final PaymentStatusLifecycle statusLifecycle;

    private final PaymentMetrics paymentMetrics;

    private final OutboxService outboxService;


    /**
     * Transaction #1
     *
     * Creates the payment in PENDING state and commits it.
     */
    @Transactional
    public PaymentResponse createPendingPayment(CreatePaymentRequest request,
                                                OrderResponse order,
                                                String providerName) {

        if (paymentRepository.existsByOrderId(request.orderId())) {

            paymentMetrics.duplicatePayment();

            throw new DuplicatePaymentException(
                    request.orderId()
            );
        }

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .amount(order.getTotalAmount())
                .currency(request.currency())
                .paymentMethod(request.paymentMethod())
                .provider(providerName)
                .status(PaymentStatus.PENDING)
                .build();

        try {

            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            paymentMetrics.paymentCreated();

            return paymentMapper.toResponse(savedPayment);

        } catch (DataIntegrityViolationException ex) {

            paymentMetrics.duplicatePayment();

            throw new PaymentConcurrencyException(
                    request.orderId()
            );
        }
    }


    /**
     * Transaction #2
     *
     * Updates the payment after the external provider
     * has responded.
     */
    @Transactional
    public PaymentResponse completePayment(Long paymentId,
                                           PaymentProviderResponse providerResponse) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        paymentId
                                )
                        );

        payment.setProviderReference(
                providerResponse.providerReference()
        );

        transitionStatus(payment, providerResponse.status());

        if (providerResponse.failureReason() != null) {
            payment.setFailureReason(
                    providerResponse.failureReason()
            );
        }

        Payment savedPayment = paymentRepository.save(payment);

        PaymentResponse response =
                paymentMapper.toResponse(savedPayment);

        if (response.status() == PaymentStatus.SUCCESS) {

            writePaymentOutbox(
                    EventType.PAYMENT_SUCCESSFUL,
                    response,
                    null
            );

        } else if (response.status() == PaymentStatus.FAILED) {

            writePaymentOutbox(
                    EventType.PAYMENT_FAILED,
                    response,
                    response.failureReason()
            );
        }

        return response;
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

    private void writePaymentOutbox(EventType eventType,
                                    PaymentResponse payment,
                                    String failureReason) {

        PaymentCompletedEvent outboxEvent =
                PaymentCompletedEvent.builder()
                        .eventType(eventType)
                        .paymentId(payment.id())
                        .orderId(payment.orderId())
                        .amount(payment.amount())
                        .currency(payment.currency())
                        .paymentMethod(payment.paymentMethod())
                        .paymentStatus(payment.status())
                        .transactionId(payment.providerReference())
                        .failureReason(failureReason)
                        .build();

        outboxService.savePaymentCompletedEvent(outboxEvent);
    }
}