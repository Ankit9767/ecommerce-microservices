package com.example.auth_service.service;

import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(
            User user,
            String token
    );

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(User user);

}