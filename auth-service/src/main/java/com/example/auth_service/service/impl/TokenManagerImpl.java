package com.example.auth_service.service.impl;

import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.security.CustomUserDetails;

import com.example.auth_service.security.jwt.JwtProperties;
import com.example.auth_service.security.jwt.JwtService;
import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.service.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenManagerImpl implements TokenManager {

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final JwtProperties jwtProperties;

    @Override
    public AuthResponse generateTokens(User user) {

        String accessToken = jwtService.generateAccessToken(new CustomUserDetails(user));

        String refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getExpiration())
                .build();
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {

        RefreshToken storedToken = refreshTokenService.verifyRefreshToken(refreshToken);

        User user = storedToken.getUser();

        refreshTokenService.revokeRefreshToken(refreshToken);

        return generateTokens(user);

    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    public void revokeAllTokens(User user) {
        refreshTokenService.revokeAllUserTokens(user);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}