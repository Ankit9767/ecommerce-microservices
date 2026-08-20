package com.ecommerce.common.events;

import com.ecommerce.common.kafka.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductCreatedEvent extends ProductEvent {

    public static ProductEvent of(Long productId, String name, String sku,
                                  String category, BigDecimal price, Boolean active) {
        return ProductCreatedEvent.builder()
                .eventType(EventType.PRODUCT_CREATED)
                .productId(productId)
                .name(name)
                .sku(sku)
                .category(category)
                .price(price)
                .active(active)
                .build();
    }
}