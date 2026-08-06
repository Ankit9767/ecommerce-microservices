package com.example.order_service.dto;


import com.ecommerce.common.enums.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotBlank(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long productId, Long customerId, Integer quantity, PaymentMethod paymentMethod) {
        this.productId = productId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.paymentMethod = paymentMethod;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
