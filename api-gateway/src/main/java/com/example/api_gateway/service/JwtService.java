package com.example.api_gateway.service;

import com.ecommerce.common.security.JwtClaimsExtractor;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private JwtClaimsExtractor claimsExtractor;

    @PostConstruct
    public void init() {

        SecretKey signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        claimsExtractor = new JwtClaimsExtractor(signingKey);
    }

    public String extractUsername(String token) {

        return claimsExtractor.extractUsername(token);
    }

    public Long extractUserId(String token) {

        return claimsExtractor.extractUserId(token);
    }

    public String extractEmail(String token) {

        return claimsExtractor.extractEmail(token);
    }


    public List<String> extractRoles(String token) {

        return claimsExtractor.extractRoles(token);
    }

    public Date extractExpiration(String token) {

        return claimsExtractor.extractExpiration(token);
    }

    public boolean isTokenExpired(String token) {

        return claimsExtractor.isTokenExpired(token);
    }

    public boolean isTokenValid(String token) {

        try {
            return !claimsExtractor.isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }
}