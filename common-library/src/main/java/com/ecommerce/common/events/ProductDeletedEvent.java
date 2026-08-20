package com.ecommerce.common.events;

import com.ecommerce.common.kafka.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProductDeletedEvent extends ProductEvent {

    public static ProductEvent of(Long productId) {
        return ProductDeletedEvent.builder()
                .eventType(EventType.PRODUCT_DELETED)
                .productId(productId)
                .active(false)
                .build();
    }
}