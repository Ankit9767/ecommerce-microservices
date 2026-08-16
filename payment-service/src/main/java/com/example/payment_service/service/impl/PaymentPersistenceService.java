package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.dto.CreatePaymentRequest;
import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.DuplicatePaymentException;
import com.example.payment_service.exception.InvalidPaymentStatusTransitionException;
import com.example.payment_service.exception.PaymentConcurrencyException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.metrics.PaymentMetrics;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentProvider;
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

    private final PaymentProvider paymentProvider;

    private final PaymentStatusLifecycle statusLifecycle;

    private final PaymentMetrics paymentMetrics;


    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request,
                                         OrderResponse order) {

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
                .provider(paymentProvider.getProviderName())
                .status(PaymentStatus.PENDING)
                .build();

        try {
            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            PaymentProviderRequest providerRequest =
                    new PaymentProviderRequest(
                            savedPayment.getId(),
                            savedPayment.getOrderId(),
                            savedPayment.getAmount(),
                            savedPayment.getCurrency(),
                            savedPayment.getPaymentMethod()
                    );

            PaymentProviderResponse providerResponse =
                    paymentProvider.createPayment(providerRequest);

            savedPayment.setProviderReference(providerResponse.providerReference());

            transitionStatus(savedPayment, providerResponse.status());

            if (providerResponse.failureReason() != null) {
                savedPayment.setFailureReason(
                        providerResponse.failureReason()
                );
            }

            Payment finalPayment = paymentRepository.save(savedPayment);

            paymentMetrics.paymentCreated();

            return paymentMapper.toResponse(finalPayment);

        } catch (DataIntegrityViolationException ex) {
            paymentMetrics.duplicatePayment();
            throw new PaymentConcurrencyException(
                    request.orderId()
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
}
