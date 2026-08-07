package com.example.auth_service.service;

import com.example.auth_service.entity.UserSession;
import com.example.auth_service.entity.User;

public interface UserSessionService {

    String createSession(User user);

    UserSession verifySession(String refreshToken);

    void revokeSession(String refreshToken);

    void revokeAllSessions(User user);

}