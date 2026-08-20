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
public class UserRegisteredEvent extends UserEvent {

    public static UserEvent of(Long userId, String email, String username) {
        return UserRegisteredEvent.builder()
                .eventType(EventType.USER_REGISTERED)
                .userId(userId)
                .email(email)
                .username(username)
                .build();
    }
}