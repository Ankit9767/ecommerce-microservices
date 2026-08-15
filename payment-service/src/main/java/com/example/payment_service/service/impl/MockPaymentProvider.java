package com.example.payment_service.service.impl;

import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.example.payment_service.service.PaymentProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    public PaymentProviderResponse createPayment(PaymentProviderRequest request) {

        String reference = "MOCK-" + UUID.randomUUID();

        return new PaymentProviderResponse(
                reference,
                PaymentStatus.PROCESSING,
                null
        );
    }

    @Override
    public PaymentProviderResponse verifyPayment(String providerReference) {

        return new PaymentProviderResponse(
                providerReference,
                PaymentStatus.SUCCESS,
                null
        );
    }

    @Override
    public PaymentProviderResponse refundPayment(String providerReference,
                                                 BigDecimal amount) {

        return new PaymentProviderResponse(
                providerReference,
                PaymentStatus.REFUNDED,
                null
        );
    }
}