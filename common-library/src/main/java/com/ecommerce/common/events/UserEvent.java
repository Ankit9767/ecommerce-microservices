package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base contract for user life-cycle events (publish-only this milestone;
 * intended for notification / analytics).
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class UserEvent extends DomainEvent {

    private Long userId;

    private String email;

    private String username;

}