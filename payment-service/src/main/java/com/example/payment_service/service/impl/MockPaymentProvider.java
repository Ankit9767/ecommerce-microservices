package com.example.payment_service.service.impl;

import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.example.payment_service.dto.provider.PaymentProviderResponse;
import com.example.payment_service.service.PaymentProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentProviderResponse createPayment(PaymentProviderRequest request) {

        return new PaymentProviderResponse(
                "MOCK-" + request.paymentId(),
                PaymentStatus.PROCESSING
        );
    }

    @Override
    public PaymentProviderResponse verifyPayment(String providerPaymentId) {

        return new PaymentProviderResponse(
                providerPaymentId,
                PaymentStatus.SUCCESS
        );
    }

    @Override
    public PaymentProviderResponse refundPayment(String providerPaymentId,
                                                 BigDecimal amount) {

        return new PaymentProviderResponse(
                providerPaymentId,
                PaymentStatus.REFUNDED
        );
    }
}