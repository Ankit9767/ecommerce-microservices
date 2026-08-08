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
    public AuthResponse generateTokens(User user, SessionInfo sessionInfo) {

        String accessToken = jwtService.generateAccessToken(new CustomUserDetails(user), sessionInfo.getSessionId());

        String refreshToken = userSessionService.createSession(user, sessionInfo);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getExpiration())
                .build();
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {

        /*
         * 1. Verify the opaque refresh token.
         */

        UserSession storedSession  = userSessionService.verifySession(refreshToken);

        User user = storedSession.getUser();

        /*
         * 2. IMPORTANT:
         *
         * Reuse the existing session ID.
         *
         * We do NOT generate a new session ID.
         */

        String existingSessionId  = storedSession.getSessionId();

        /*
         * 3. Preserve the existing device/session
         * information during token rotation.
         */


        SessionInfo sessionInfo =
                SessionInfo.builder()

                        .sessionId(existingSessionId )

                        .deviceName(
                                storedSession.getDeviceName()
                        )

                        .deviceType(
                                storedSession.getDeviceType()
                        )

                        .browser(
                                storedSession.getBrowser()
                        )

                        .operatingSystem(
                                storedSession.getOperatingSystem()
                        )

                        .ipAddress(
                                storedSession.getIpAddress()
                        )

                        .build();

        /*
         * 4. Revoke the old refresh token.
         */

        userSessionService.revokeSession(refreshToken);

        /*
         * 5. Generate the new tokens using
         * the SAME session ID.
         */

        return generateTokens(user, sessionInfo);

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