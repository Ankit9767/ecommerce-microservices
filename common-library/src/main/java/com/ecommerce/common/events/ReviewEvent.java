package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public abstract class ReviewEvent extends DomainEvent {

    private Long reviewId;

    private Long productId;

    private Long userId;

    private Long orderId;
}