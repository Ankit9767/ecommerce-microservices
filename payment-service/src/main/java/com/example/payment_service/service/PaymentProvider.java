package com.example.payment_service.service;

import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.example.payment_service.dto.provider.PaymentProviderResponse;

import java.math.BigDecimal;

public interface PaymentProvider {

    PaymentProviderResponse createPayment(PaymentProviderRequest request);

    PaymentProviderResponse verifyPayment(String providerPaymentId);

    PaymentProviderResponse refundPayment(String providerPaymentId, BigDecimal amount);
}
