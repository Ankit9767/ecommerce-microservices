package com.example.api_gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessTokenValidator {

    private final JwtConfig jwtConfig;

    public void validateAccessToken(Jwt jwt) {

        String tokenType = jwt.getClaimAsString("TOKEN_TYPE");

        if (!"ACCESS".equals(tokenType)) {
            throw new JwtException("Invalid token type");
        }
    }
}