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
public class ProductPrimaryImageChangedEvent extends ProductEvent {

    private Long imageId;

    private String imageUrl;

    public static ProductPrimaryImageChangedEvent of(Long productId,
                                                     Long imageId,
                                                     String imageUrl) {

        return ProductPrimaryImageChangedEvent.builder()
                .eventType(EventType.PRODUCT_PRIMARY_IMAGE_CHANGED)
                .productId(productId)
                .imageId(imageId)
                .imageUrl(imageUrl)
                .build();
    }
}