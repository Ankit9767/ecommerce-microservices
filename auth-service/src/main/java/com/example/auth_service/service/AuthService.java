package com.example.auth_service.service;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request, HttpServletRequest servletRequest);

    AuthResponse login(LoginRequest loginRequest, HttpServletRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    List<SessionResponse> getSessions(String accessToken);

    void logoutSession(Long id);

    void logoutAllSessions();

}
