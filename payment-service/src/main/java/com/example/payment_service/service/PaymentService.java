package com.example.payment_service.service;

import com.ecommerce.common.dto.PaymentResponse;
import com.example.payment_service.dto.CreatePaymentRequest;
import org.springframework.security.core.Authentication;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request,
                                  Authentication authentication);
}