package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class ReviewCreatedEvent extends ReviewEvent {

    private Integer rating;

    private String title;

    private String comment;
}
