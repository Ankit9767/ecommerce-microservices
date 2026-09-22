package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentCheckoutResponse;
import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.enums.PaymentStatus;
import com.ecommerce.common.events.OrderCancelledEvent;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.exception.RemoteResourceNotFoundException;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.security.CurrentUser;
import com.ecommerce.common.security.RoleSecurity;
import com.example.payment_service.client.OrderClient;
import com.example.payment_service.config.RazorpayProperties;
import com.example.payment_service.dto.CreatePaymentRequest;
import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.*;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.metrics.PaymentMetrics;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.*;
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

    private final PaymentEventFactory paymentEventFactory;

    private final OutboxService outboxService;

    private final RazorpayProperties razorpayProperties;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request,
                                         Authentication authentication) {

        Long currentUserId = currentUser.getUserId(authentication);

        OrderResponse order;

        try {

            order = orderClient.getOrderInternal(request.orderId());

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

        return processPayment(order);
    }


    private void createPaymentInternal(CreatePaymentRequest request) {

        OrderResponse order;

        try {

            order = orderClient.getOrderInternal(request.orderId());

        } catch (RemoteResourceNotFoundException ex) {

            throw new OrderNotFoundException(
                    request.orderId()
            );
        }

        processPayment(order);
    }


    private PaymentResponse processPayment(OrderResponse order) {

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {

            throw new PaymentAlreadyCancelledException(
                    order.getId()
            );
        }

        /*
         * --------------------------------------------------
         * ORDER PAYMENT DETAILS
         *
         * Order Service is the source of truth.
         * --------------------------------------------------
         */

        if (order.getTotalAmount() == null) {

            throw new MissingPaymentDetailsException(
                    order.getId(),
                    "totalAmount"
            );
        }

        if (order.getCurrency() == null) {

            throw new MissingPaymentDetailsException(
                    order.getId(),
                    "currency"
            );
        }

        if (order.getPaymentMethod() == null) {

            throw new MissingPaymentDetailsException(
                    order.getId(),
                    "paymentMethod"
            );
        }

        /*
         * --------------------------------------------------
         * TRANSACTION #1
         *
         * Create or retrieve the local PENDING payment.
         * --------------------------------------------------
         */

        PaymentPersistenceService.PaymentCreationResult creationResult =
                paymentPersistenceService.createPendingPayment(
                        order,
                        paymentProvider.getProviderName()
                );

        PaymentResponse pendingPayment = creationResult.payment();

        /*
         * --------------------------------------------------
         * EXISTING PAYMENT
         *
         * Never blindly create another provider transaction.
         * --------------------------------------------------
         */

        if (!creationResult.created()) {

            Payment existingPayment =
                    paymentRepository.findById(
                            pendingPayment.id()
                    ).orElseThrow(() ->
                            new PaymentNotFoundException(
                                    pendingPayment.id()
                            )
                    );

            PaymentStatus status = existingPayment.getStatus();

            log.info(
                    "Payment already exists for order {}. " +
                            "paymentId={}, status={}, provider={}, " +
                            "providerOrderId={}, providerPaymentId={}",
                    order.getId(),
                    existingPayment.getId(),
                    status,
                    existingPayment.getProvider(),
                    existingPayment.getProviderOrderId(),
                    existingPayment.getProviderPaymentId()
            );

            /*
             * --------------------------------------------------
             * TERMINAL STATES
             *
             * Never create another provider transaction.
             * --------------------------------------------------
             */

            if (status == PaymentStatus.SUCCESS ||
                    status == PaymentStatus.FAILED ||
                    status == PaymentStatus.CANCELLED ||
                    status == PaymentStatus.REFUNDED) {

                return paymentMapper.toResponse(existingPayment);
            }

            /*
             * --------------------------------------------------
             * PROVIDER ORDER ALREADY EXISTS
             *
             * Razorpay order has already been created.
             *
             * Reuse it.
             *
             * This is important for idempotency.
             * --------------------------------------------------
             */

            if (existingPayment.getProviderOrderId() != null &&
                    !existingPayment.getProviderOrderId().isBlank()) {

                log.info(
                        "Provider order already exists for payment {}. " +
                                "Reusing providerOrderId={}",
                        existingPayment.getId(),
                        existingPayment.getProviderOrderId()
                );

                return paymentMapper.toResponse(
                        existingPayment
                );
            }

            /*
             * --------------------------------------------------
             * PENDING WITHOUT PROVIDER ORDER
             *
             * The local payment exists, but provider order creation
             * previously failed.
             *
             * It is safe to retry provider creation.
             * --------------------------------------------------
             */

            log.info(
                    "Payment {} is PENDING without a provider order. " +
                            "Retrying provider order creation.",
                    existingPayment.getId()
            );
        }

        /*
         * --------------------------------------------------
         * PROVIDER REQUEST
         * --------------------------------------------------
         */

        PaymentProviderRequest providerRequest =
                new PaymentProviderRequest(
                        pendingPayment.id(),
                        pendingPayment.orderId(),
                        pendingPayment.amount(),
                        pendingPayment.currency(),
                        pendingPayment.paymentMethod()
                );

        PaymentProviderResponse providerResponse;

        try {

            providerResponse =
                    paymentProvider.createProviderPaymentTransaction(
                            providerRequest
                    );

        } catch (RuntimeException ex) {

            log.error(
                    "Payment provider failed: paymentId={}, orderId={}",
                    pendingPayment.id(),
                    pendingPayment.orderId(),
                    ex
            );

            /*
             * Keep local payment as PENDING.
             *
             * The next attempt can retry provider creation.
             */
            throw ex;
        }

        /*
         * --------------------------------------------------
         * TRANSACTION #2
         *
         * Persist provider identifiers.
         *
         * For Razorpay order creation:
         *
         * providerOrderId   = order_xxx
         * providerPaymentId = null
         * providerReference = order_xxx
         * status            = PENDING
         * --------------------------------------------------
         */

        return paymentPersistenceService.markPaymentProcessing(
                pendingPayment.id(),
                providerResponse
        );
    }

    @Override
    public void processOrderCreatedEvent(OrderEvent event) {

        /*
         * Only newly-created orders trigger automatic
         * payment creation.
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
                        orderEvent.getOrderId()
                );

        try {

            createPaymentInternal(request);

        } catch (Exception ex) {

            log.error(
                    "Failed to auto-create payment for order {}",
                    event.getOrderId(), ex);

            throw ex;
        }
    }

    @Override
    @Transactional
    public void processOrderCancelledEvent(OrderCancelledEvent event) {

        Long orderId = event.getOrderId();

        Payment payment =
                paymentRepository.findByOrderId(orderId)
                        .orElse(null);

        if (payment == null) {

            log.info(
                    "No payment found for cancelled order {}",
                    orderId
            );

            return;
        }

        PaymentStatus currentStatus = payment.getStatus();

        if (currentStatus == PaymentStatus.SUCCESS) {

            log.warn(
                    "Order {} was cancelled but payment {} is already SUCCESS. " +
                            "Refund handling is required.",
                    orderId,
                    payment.getId()
            );

            return;
        }

        if (currentStatus == PaymentStatus.FAILED) {

            log.info(
                    "Payment {} for order {} is already FAILED. " +
                            "Ignoring duplicate cancellation.",
                    payment.getId(),
                    orderId
            );

            return;
        }

        transitionStatus(
                payment,
                PaymentStatus.FAILED
        );

        payment.setFailureReason(
                event.getReason() != null
                        ? event.getReason()
                        : "Order cancelled"
        );

        try {

            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            outboxService.savePaymentCompletedEvent(
                    paymentEventFactory.createPaymentEvent(
                            EventType.PAYMENT_FAILED,
                            savedPayment
                    )
            );

            log.info(
                    "Payment {} for order {} marked FAILED because order was cancelled",
                    savedPayment.getId(),
                    orderId
            );

        } catch (ObjectOptimisticLockingFailureException ex) {

            paymentMetrics.concurrentModification();

            log.warn(
                    "Concurrent modification while cancelling payment {} " +
                            "for order {}",
                    payment.getId(),
                    orderId
            );

            throw new PaymentConcurrentModificationException(
                    payment.getId()
            );
        }
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
                            return new PaymentNotFoundException(
                                    paymentId
                            );
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

        if (currentStatus == targetStatus) {
            return;
        }

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

    @Override
    public PaymentCheckoutResponse initializeCheckout(Long paymentId,
                                                      Authentication authentication) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    paymentMetrics.paymentNotFound();
                    return new PaymentNotFoundException(paymentId);
                });

        if (roleSecurity.hasRole(authentication, "ADMIN")) {

            return initializeCheckoutForPayment(payment);
        }

        Long currentUserId = currentUser.getUserId(authentication);

        if (!payment.getCustomerId().equals(currentUserId)) {

            throw new AccessDeniedException(
                    "You are not authorized to initialize checkout for this payment"
            );
        }

        return initializeCheckoutForPayment(payment);
    }

    private PaymentCheckoutResponse initializeCheckoutForPayment(Payment payment) {

        if (payment.getStatus() != PaymentStatus.PENDING) {

            throw new IllegalStateException(
                    "Checkout can only be initialized for a PENDING payment"
            );
        }

        if (payment.getProviderOrderId() == null ||
                payment.getProviderOrderId().isBlank()) {

            log.info(
                    "Provider order missing for payment {}. Reinitializing provider checkout.",
                    payment.getId()
            );

            OrderResponse order;

            try {

                order = orderClient.getOrderInternal(payment.getOrderId());

            } catch (RemoteResourceNotFoundException ex) {

                throw new OrderNotFoundException(payment.getOrderId());
            }

            /*
             * processPayment() detects the existing payment and creates
             * a provider order only when providerOrderId is missing.
             */
            processPayment(order);

            Long paymentId = payment.getId();

            payment = paymentRepository
                    .findById(paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        }

        if (payment.getProviderOrderId() == null ||
                payment.getProviderOrderId().isBlank()) {

            throw new IllegalStateException(
                    "Payment provider order was not initialized"
            );
        }

        String providerKeyId = null;

        if ("RAZORPAY".equalsIgnoreCase(payment.getProvider())) {

            providerKeyId = razorpayProperties.getKeyId();
        }

        return new PaymentCheckoutResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getProvider(),
                payment.getProviderOrderId(),
                providerKeyId,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus()
        );
    }

    @Transactional
    @Override
    public PaymentResponse completeMockPayment(Long paymentId,
                                               Authentication authentication) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(paymentId)
                );

        if (!"MOCK".equalsIgnoreCase(payment.getProvider())) {
            throw new IllegalStateException(
                    "Payment is not using MOCK provider"
            );
        }

        if (!roleSecurity.hasRole(authentication, "ADMIN")) {

            Long currentUserId = currentUser.getUserId(authentication);

            if (!payment.getCustomerId().equals(currentUserId)) {
                throw new AccessDeniedException(
                        "You are not authorized to complete this payment"
                );
            }
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING payments can be completed"
            );
        }

        if (payment.getProviderReference() == null ||
                payment.getProviderReference().isBlank()) {

            throw new IllegalStateException(
                    "Mock provider reference is missing"
            );
        }

        PaymentProviderResponse providerResponse =
                paymentProvider.verifyPayment(
                        payment.getProviderReference()
                );

        transitionStatus(payment, providerResponse.status());

        payment.setProviderPaymentId(
                providerResponse.providerPaymentId()
        );

        payment.setProviderReference(
                providerResponse.providerReference()
        );

        Payment savedPayment;

        try {

            savedPayment = paymentRepository.saveAndFlush(payment);

        } catch (ObjectOptimisticLockingFailureException ex) {

            paymentMetrics.concurrentModification();

            throw new PaymentConcurrentModificationException(
                    paymentId
            );
        }

        outboxService.savePaymentCompletedEvent(
                paymentEventFactory.createPaymentEvent(
                        EventType.PAYMENT_SUCCESSFUL,
                        savedPayment
                )
        );

        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional
    @Override
    public PaymentResponse failMockPayment(Long paymentId,
                                           Authentication authentication) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(paymentId)
                );

        if (!"MOCK".equalsIgnoreCase(payment.getProvider())) {
            throw new IllegalStateException(
                    "Payment is not using MOCK provider"
            );
        }

        if (!roleSecurity.hasRole(authentication, "ADMIN")) {

            Long currentUserId = currentUser.getUserId(authentication);

            if (!payment.getCustomerId().equals(currentUserId)) {
                throw new AccessDeniedException(
                        "You are not authorized to fail this payment"
                );
            }
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING payments can be failed"
            );
        }

        transitionStatus(payment, PaymentStatus.FAILED);

        payment.setFailureReason("Mock payment failed");

        Payment savedPayment;

        try {

            savedPayment = paymentRepository.saveAndFlush(payment);

        } catch (ObjectOptimisticLockingFailureException ex) {

            paymentMetrics.concurrentModification();

            throw new PaymentConcurrentModificationException(
                    paymentId
            );
        }

        outboxService.savePaymentCompletedEvent(
                paymentEventFactory.createPaymentEvent(
                        EventType.PAYMENT_FAILED,
                        savedPayment
                )
        );

        return paymentMapper.toResponse(savedPayment);
    }
}