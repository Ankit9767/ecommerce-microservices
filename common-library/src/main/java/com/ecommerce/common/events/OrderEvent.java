package com.ecommerce.common.events;

import com.ecommerce.common.enums.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class OrderEvent extends DomainEvent {

    private Long orderId;

    private Long customerId;

    private Currency currency;

}