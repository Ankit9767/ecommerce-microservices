package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base contract for cart events (publish-only this milestone; intended for
 * marketing / analytics / notifications).
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class CartEvent extends DomainEvent {

    private Long customerId;

}