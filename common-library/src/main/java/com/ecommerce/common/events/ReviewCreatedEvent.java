package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class ReviewCreatedEvent extends DomainEvent {

    private Long reviewId;
    private Long productId;
    private Long userId;
    private Long orderId;
    private Integer rating;
    private String title;
    private String comment;
}
