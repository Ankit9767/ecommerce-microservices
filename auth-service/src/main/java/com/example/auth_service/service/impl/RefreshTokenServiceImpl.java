package com.example.auth_service.service.impl;

import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.InvalidRefreshTokenException;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.security.TokenHashService;
import com.example.auth_service.security.jwt.JwtProperties;
import com.example.auth_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository repository;

    private final JwtProperties jwtProperties;

    private final TokenHashService tokenHashService;

    @Override
    public RefreshToken createRefreshToken(
            User user,
            String token
    ) {

        RefreshToken refreshToken =
                RefreshToken.builder()

                        .tokenHash(tokenHashService.hash(token))

                        .user(user)

                        .expiryDate(
                                Instant.now()
                                        .plusMillis(jwtProperties.getRefreshExpiration())
                        )

                        .revoked(false)

                        .build();

        return repository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        String tokenHash = tokenHashService.hash(token);

        RefreshToken refreshToken =
                repository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Refresh token not found"
                                )
                        );

        if (refreshToken.getRevoked()) {

            throw new InvalidRefreshTokenException(
                    "Refresh token revoked"
            );
        }

        if (refreshToken.getExpiryDate()
                .isBefore(Instant.now())) {

            repository.delete(refreshToken);

            throw new InvalidRefreshTokenException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {

        String tokenHash = tokenHashService.hash(token);

        repository.findByTokenHash(tokenHash)

                .ifPresent(refreshToken -> {

                    refreshToken.setRevoked(true);

                    repository.save(refreshToken);

                });

    }

    @Override
    public void revokeAllUserTokens(User user) {

        repository.findByUser(user)

                .forEach(refreshToken -> {

                    refreshToken.setRevoked(true);

                    repository.save(refreshToken);

                });

    }

}