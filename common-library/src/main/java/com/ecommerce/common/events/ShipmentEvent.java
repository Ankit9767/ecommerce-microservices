package com.ecommerce.common.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = ShipmentCreatedEvent.class,
                name = "shipment-created"
        ),
        @JsonSubTypes.Type(
                value = ShipmentShippedEvent.class,
                name = "shipment-shipped"
        ),
        @JsonSubTypes.Type(
                value = ShipmentInTransitEvent.class,
                name = "shipment-in-transit"
        ),
        @JsonSubTypes.Type(
                value = ShipmentOutForDeliveryEvent.class,
                name = "shipment-out-for-delivery"
        ),
        @JsonSubTypes.Type(
                value = ShipmentDeliveredEvent.class,
                name = "shipment-delivered"
        ),
        @JsonSubTypes.Type(
                value = ShipmentFailedEvent.class,
                name = "shipment-failed"
        ),
        @JsonSubTypes.Type(
                value = ShipmentCancelledEvent.class,
                name = "shipment-cancelled"
        )
})
public abstract class ShipmentEvent extends DomainEvent {

    private Long shipmentId;

    private Long orderId;

    private Long customerId;

    private String recipientEmail;

    private String carrier;

    private String trackingNumber;
}