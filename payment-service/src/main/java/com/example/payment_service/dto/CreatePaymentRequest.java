package com.example.payment_service.dto;

import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}