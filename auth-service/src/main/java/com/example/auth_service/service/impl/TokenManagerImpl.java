package com.example.auth_service.service.impl;

import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.entity.UserSession;
import com.example.auth_service.entity.User;
import com.example.auth_service.security.CustomUserDetails;

import com.example.auth_service.security.jwt.JwtProperties;
import com.example.auth_service.security.jwt.JwtService;
import com.example.auth_service.service.UserSessionService;
import com.example.auth_service.service.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenManagerImpl implements TokenManager {

    private final JwtService jwtService;

    private final UserSessionService userSessionService;

    private final JwtProperties jwtProperties;

    @Override
    public AuthResponse generateTokens(User user) {

        String accessToken = jwtService.generateAccessToken(new CustomUserDetails(user));

        // for now adding this as unknown , later will populate this by http headers
        SessionInfo sessionInfo =
                SessionInfo.builder()
                        .deviceName("Unknown")
                        .browser("Unknown")
                        .operatingSystem("Unknown")
                        .ipAddress("Unknown")
                        .build();

        String refreshToken = userSessionService.createSession(user, sessionInfo);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getExpiration())
                .build();
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {

        UserSession storedToken = userSessionService.verifySession(refreshToken);

        User user = storedToken.getUser();

        userSessionService.revokeSession(refreshToken);

        return generateTokens(user);

    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        userSessionService.revokeSession(refreshToken);
    }

    @Override
    public void revokeAllTokens(User user) {
        userSessionService.revokeAllSessions(user);
    }

    @Override
    public void logout(String refreshToken) {
        userSessionService.revokeSession(refreshToken);
    }
}