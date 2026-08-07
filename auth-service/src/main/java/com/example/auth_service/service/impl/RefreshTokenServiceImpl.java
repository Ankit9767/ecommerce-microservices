package com.example.auth_service.service.impl;

import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.InvalidRefreshTokenException;
import com.example.auth_service.exception.TokenReuseDetectedException;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.security.refresh.RefreshTokenGenerator;
import com.example.auth_service.security.refresh.TokenHashService;
import com.example.auth_service.security.jwt.JwtProperties;
import com.example.auth_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtProperties jwtProperties;

    private final TokenHashService tokenHashService;

    private final RefreshTokenGenerator refreshTokenGenerator;

    @Override
    public String createRefreshToken(User user) {

        String token = refreshTokenGenerator.generate();

        String tokenHash = tokenHashService.hash(token);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiryDate(
                        Instant.now().plusMillis(jwtProperties.getRefreshExpiration())
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        String hash = tokenHashService.hash(token);

        RefreshToken refreshToken =
                refreshTokenRepository.findByTokenHash(hash)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.getRevoked()) {

            revokeAllUserTokens(refreshToken.getUser());

            throw new TokenReuseDetectedException(
                    "Refresh token reuse detected"
            );

        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new InvalidRefreshTokenException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {

        String hash = tokenHashService.hash(token);

        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(session -> {

                    session.setRevoked(true);

                    refreshTokenRepository.save(session);

                });

    }

    @Override
    public void revokeAllUserTokens(User user) {

        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);

        tokens.forEach(token ->

                token.setRevoked(true)

        );

        refreshTokenRepository.saveAll(tokens);

    }

}