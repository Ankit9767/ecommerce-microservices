package com.ecommerce.common.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Base contract for product life-cycle events (publish-only this milestone;
 * intended for search / recommendations / analytics).
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ProductEvent extends DomainEvent {

    private Long productId;

    private String name;

    private String sku;

    private String category;

    private BigDecimal price;

    private Boolean active;

}