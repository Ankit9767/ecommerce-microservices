package com.example.auth_service.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenGenerator tokenGenerator;

    private final JwtTokenValidator tokenValidator;

    public String generateAccessToken(UserDetails userDetails, String sessionId) {
        return tokenGenerator.generateAccessToken(userDetails, sessionId);
    }

    public String extractUsername(String token) {
        return tokenValidator.extractUsername(token);
    }

    public List<String> extractRoles(String token) {
        return tokenValidator.extractRoles(token);
    }

    public JwtTokenType extractTokenType(String token) {
        return tokenValidator.extractTokenType(token);
    }

    public boolean isTokenExpired(String token) {
        return tokenValidator.isTokenExpired(token);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return tokenValidator.isTokenValid(token, userDetails);
    }
}