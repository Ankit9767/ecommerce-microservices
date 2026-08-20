package com.ecommerce.common.events;

import com.ecommerce.common.kafka.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Published when a user account is blocked (publish-only this milestone).
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserBlockedEvent extends UserEvent {

    public static UserEvent of(Long userId, String email, String username) {
        return UserBlockedEvent.builder()
                .eventType(EventType.USER_BLOCKED)
                .userId(userId)
                .email(email)
                .username(username)
                .build();
    }
}