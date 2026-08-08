package com.example.auth_service.service;

import com.example.auth_service.dto.response.SessionResponse;
import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.entity.UserSession;
import com.example.auth_service.entity.User;
import java.util.List;

public interface UserSessionService {

    String createSession(User user, SessionInfo sessionInfo);

    UserSession verifySession(String refreshToken);

    void revokeSession(String refreshToken);

    void revokeAllSessions(User user);

    List<SessionResponse> getSessions(User user, String currentSessionId);

    void revokeSession(Long sessionId, User user);

}