package com.ecommerce.common.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProductCreatedEvent.class, name = "product-created"),
        @JsonSubTypes.Type(value = ProductUpdatedEvent.class, name = "product-updated"),
        @JsonSubTypes.Type(value = ProductDeletedEvent.class, name = "product-deleted")
})
public abstract class ProductEvent extends DomainEvent {

    private Long productId;

    private String name;

    private String sku;

    private String category;

    private BigDecimal price;

    private Boolean active;

}