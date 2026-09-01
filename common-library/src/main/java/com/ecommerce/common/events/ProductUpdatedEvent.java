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
public class ProductUpdatedEvent extends ProductEvent {

    public static ProductUpdatedEvent of(Long productId,
                                         String name,
                                         String sku,
                                         String category,
                                         BigDecimal price,
                                         Boolean active) {

        return ProductUpdatedEvent.builder()
                .eventType(EventType.PRODUCT_UPDATED)
                .productId(productId)
                .name(name)
                .sku(sku)
                .category(category)
                .price(price)
                .active(active)
                .build();
    }
}