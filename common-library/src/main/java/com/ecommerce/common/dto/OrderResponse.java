package com.ecommerce.common.dto;

import com.ecommerce.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderResponse {

    private Long id;
    private Long productId;
    private Long customerId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;

    public OrderResponse() {
    }

    public OrderResponse(Long id,
                         Long productId,
                         Long customerId,
                         Integer quantity,
                         BigDecimal totalAmount,
                         OrderStatus status,
                         Instant createdAt) {
        this.id = id;
        this.productId = productId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
