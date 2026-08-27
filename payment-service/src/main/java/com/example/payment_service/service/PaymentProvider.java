package com.example.payment_service.service;

import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.ecommerce.common.dto.PaymentProviderResponse;

import java.math.BigDecimal;

public interface PaymentProvider {

    String getProviderName();

    PaymentProviderResponse createProviderPaymentTransaction(PaymentProviderRequest request);

    PaymentProviderResponse verifyPayment(String providerReference);

    PaymentProviderResponse refundPayment(String providerReference, BigDecimal amount);
}