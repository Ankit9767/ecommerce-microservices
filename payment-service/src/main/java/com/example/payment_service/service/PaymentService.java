package com.example.payment_service.service;

import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.dto.CreatePaymentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request,
                                  Authentication authentication);

    PaymentResponse getPayment(Long id, Authentication authentication);

    Page<PaymentResponse> getMyPayments(Authentication authentication,
                                        Pageable pageable);

    Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable);

    Page<PaymentResponse> getAllPayments(Pageable pageable);
}