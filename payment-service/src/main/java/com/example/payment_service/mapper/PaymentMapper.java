package com.example.payment_service.mapper;

import com.ecommerce.common.dto.PaymentCheckoutResponse;
import com.ecommerce.common.dto.PaymentResponse;
import com.example.payment_service.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getProvider(),
                payment.getProviderReference(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    public PaymentCheckoutResponse toCheckoutResponse(Payment payment,
                                                      String providerKeyId) {

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
}