package com.ecommerce.common.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserRegisteredEvent.class, name = "user-registered"),
        @JsonSubTypes.Type(value = UserBlockedEvent.class, name = "user-blocked"),
        @JsonSubTypes.Type(value = UserDeletedEvent.class, name = "user-deleted")
})
public abstract class UserEvent extends DomainEvent {

    private Long userId;

    private String email;

    private String username;

}