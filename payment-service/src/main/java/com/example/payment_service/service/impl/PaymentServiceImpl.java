package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.security.RoleSecurity;
import com.example.payment_service.client.OrderClient;
import com.example.payment_service.dto.CreatePaymentRequest;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.*;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.metrics.PaymentMetrics;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentService;
import com.ecommerce.common.enums.PaymentStatus;
import com.ecommerce.common.security.CurrentUser;
import com.example.payment_service.service.PaymentStatusLifecycle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final OrderClient orderClient;

    private final PaymentMapper paymentMapper;

    private final CurrentUser currentUser;

    private final RoleSecurity roleSecurity;

    private final PaymentStatusLifecycle statusLifecycle;

    private final PaymentPersistenceService paymentPersistenceService;

    private final PaymentMetrics paymentMetrics;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request,
                                         Authentication authentication) {

        Long currentUserId = currentUser.getUserId(authentication);

        OrderResponse order = orderClient.getOrder(request.orderId());

        if (order == null) {
            paymentMetrics.paymentNotFound();
            throw new OrderNotFoundException(
                    request.orderId()
            );
        }

        if (!order.getCustomerId().equals(currentUserId)) {
            paymentMetrics.orderAccessDenied();
            throw new PaymentOrderAccessDeniedException(
                    order.getId()
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new PaymentAlreadyCancelledException(
                    order.getId()
            );
        }

        return paymentPersistenceService.createPayment(request, order);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id, Authentication authentication) {

        Payment payment =
                paymentRepository.findById(id)
                        .orElseThrow(() -> {
                            paymentMetrics.paymentNotFound();
                            return new PaymentNotFoundException(id);
                        });

        if (roleSecurity.hasRole(authentication, "ADMIN")) {
            return paymentMapper.toResponse(payment);
        }

        Long currentUserId = currentUser.getUserId(authentication);

        if (!payment.getCustomerId().equals(currentUserId)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this payment"
            );
        }

        paymentMetrics.paymentViewed();

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {

        return paymentRepository
                .findAll(pageable)
                .map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(Authentication authentication,
                                               Pageable pageable) {

        Long currentUserId = currentUser.getUserId(authentication);

        return paymentRepository
                .findByCustomerId(
                        currentUserId,
                        pageable
                )
                .map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {

        return paymentRepository
                .findByStatus(
                        status,
                        pageable
                )
                .map(paymentMapper::toResponse);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId,
                                               PaymentStatus targetStatus) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() -> {
                            paymentMetrics.paymentNotFound();
                            return new PaymentNotFoundException(paymentId);
                        });

        transitionStatus(payment, targetStatus);

        try {

            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            return paymentMapper.toResponse(savedPayment);

        } catch (ObjectOptimisticLockingFailureException ex) {

            paymentMetrics.concurrentModification();
            throw new PaymentConcurrentModificationException(
                    paymentId
            );
        }
    }

    private void transitionStatus(Payment payment,
                                  PaymentStatus targetStatus) {

        PaymentStatus currentStatus = payment.getStatus();

        if (!statusLifecycle.canTransition(currentStatus,
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