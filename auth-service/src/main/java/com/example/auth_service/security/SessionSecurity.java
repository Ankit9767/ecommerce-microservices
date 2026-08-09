package com.example.auth_service.security;

import com.example.auth_service.entity.UserSession;
import com.example.auth_service.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("sessionSecurity")
@RequiredArgsConstructor
public class SessionSecurity {

    private final UserSessionRepository userSessionRepository;

    public boolean isOwner(Long sessionId,
            String username) {

        return userSessionRepository
                .findById(sessionId)
                .map(UserSession::getUser)
                .map(user ->
                        user.getUsername()
                                .equals(username)
                )
                .orElse(false);
    }
}