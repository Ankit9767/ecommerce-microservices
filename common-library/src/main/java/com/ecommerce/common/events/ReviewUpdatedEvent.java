package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class ReviewUpdatedEvent extends ReviewEvent {

    private Integer oldRating;

    private Integer newRating;

    private String title;

    private String comment;
}