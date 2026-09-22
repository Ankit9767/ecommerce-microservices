package com.example.shipping_service.dto;

import com.example.shipping_service.enums.ShipmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentResponse {

    private Long id;

    private Long orderId;

    private Long customerId;

    private String recipientEmail;

    private String shippingRecipientName;

    private String shippingPhone;

    private String shippingAddressLine1;

    private String shippingAddressLine2;

    private String shippingCity;

    private String shippingState;

    private String shippingPostalCode;

    private String shippingCountry;

    private ShipmentStatus status;

    private String trackingNumber;

    private String carrier;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}