package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.enums.PaymentStatus;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.exception.RemoteResourceNotFoundException;
import com.ecommerce.common.security.RoleSecurity;
import com.example.payment_service.client.OrderClient;
import com.example.payment_service.dto.CreatePaymentRequest;
import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.*;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.metrics.PaymentMetrics;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentProvider;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.PaymentStatusLifecycle;
import com.example.payment_service.service.OutboxService;
import com.ecommerce.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    private final PaymentProvider paymentProvider;


    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request,
                                         Authentication authentication) {

        Long currentUserId = currentUser.getUserId(authentication);

        OrderResponse order;

        try {

            order = orderClient.getOrder(request.orderId());

        } catch (RemoteResourceNotFoundException ex) {

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


        // --------------------------------------------------
        // TRANSACTION #1
        // Create PENDING payment and COMMIT
        // --------------------------------------------------

        PaymentResponse pendingPayment =
                paymentPersistenceService.createPendingPayment(
                        request,
                        order,
                        paymentProvider.getProviderName()
                );


        // --------------------------------------------------
        // NO DATABASE TRANSACTION HERE
        // External Mock provider call
        // --------------------------------------------------

        PaymentProviderRequest providerRequest =
                new PaymentProviderRequest(
                        pendingPayment.id(),
                        pendingPayment.orderId(),
                        pendingPayment.amount(),
                        pendingPayment.currency(),
                        pendingPayment.paymentMethod()
                );

        PaymentProviderResponse providerResponse =
                paymentProvider.createPayment(providerRequest);

        return paymentPersistenceService.markPaymentProcessing(
                pendingPayment.id(),
                providerResponse
        );
    }

    @Override
    public void processOrderEvent(OrderEvent event) {

        /*
         * Only newly-placed orders drive payment auto-creation. Order
         * cancellations are handled by order-service and need no payment here.
         */
        if (!(event instanceof OrderCreatedEvent)) {
            log.info("Ignoring non-created order event '{}'", event.getEventType());
            return;
        }

        OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;

        if (orderEvent.getCurrency() == null) {

            throw new MissingPaymentDetailsException(
                    orderEvent.getOrderId(),
                    "currency"
            );
        }

        if (orderEvent.getPaymentMethod() == null) {

            throw new MissingPaymentDetailsException(
                    orderEvent.getOrderId(),
                    "paymentMethod"
            );
        }

        if (orderEvent.getTotalAmount() == null) {
            throw new MissingPaymentDetailsException(
                    orderEvent.getOrderId(),
                    "totalAmount"
            );
        }

        CreatePaymentRequest request =
                new CreatePaymentRequest(
                        orderEvent.getOrderId(),
                        orderEvent.getTotalAmount(),
                        orderEvent.getCurrency(),
                        orderEvent.getPaymentMethod()
                );

        try {

            createPayment(
                    request,
                    internalAuthentication(
                            orderEvent.getCustomerId()
                    )
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to auto-create payment for order {}",
                    event.getOrderId(), ex);

            throw ex;
        }
    }

    private Authentication internalAuthentication(Long customerId) {

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                customerId, null, java.util.Collections.emptyList()
        );
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