package com.example.order_service.dto;

import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOrderFromCartRequest(

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotNull(message = "Shipping address is required")
        @Valid
        ShippingAddressRequest shippingAddress
) {
}