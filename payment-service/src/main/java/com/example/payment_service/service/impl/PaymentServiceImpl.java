package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.security.RoleSecurity;
import com.example.payment_service.client.OrderClient;
import com.example.payment_service.dto.CreatePaymentRequest;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.DuplicatePaymentException;
import com.example.payment_service.exception.OrderNotFoundException;
import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.exception.PaymentOrderAccessDeniedException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentService;
import com.ecommerce.common.enums.PaymentStatus;
import com.ecommerce.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request,
                                         Authentication authentication) {

        Long currentUserId = currentUser.getUserId(authentication);

        OrderResponse order = orderClient.getOrder(request.orderId());

        if (order == null) {
            throw new OrderNotFoundException(request.orderId());
        }

        /*
         * Customer can only create a payment
         * for their own order.
         */
        if (!order.getCustomerId().equals(currentUserId)) {
            throw new PaymentOrderAccessDeniedException(order.getId());
        }

        /*
         * Prevent duplicate payment records
         * for the same order.
         */
        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new DuplicatePaymentException(request.orderId());
        }

        /*
         * The amount comes from the order,
         * not from the client request.
         */
        Payment payment = Payment.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .amount(order.getTotalAmount())
                .currency(request.currency())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id, Authentication authentication) {

        Payment payment =
                paymentRepository.findById(id)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(id)
                        );

        if (roleSecurity.hasRole(authentication, "ADMIN")) {
            return paymentMapper.toResponse(payment);
        }

        Long currentUserId = currentUser.getUserId(authentication);

        if (!payment.getCustomerId()
                .equals(currentUserId)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this payment"
            );
        }

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
}