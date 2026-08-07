package com.example.auth_service.service;

import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.entity.User;

public interface TokenManager {

    AuthResponse generateTokens(User user);

    AuthResponse refreshAccessToken(String refreshToken);

    void revokeRefreshToken(String refreshToken);

    void revokeAllTokens(User user);

    void logout(String refreshToken);

}