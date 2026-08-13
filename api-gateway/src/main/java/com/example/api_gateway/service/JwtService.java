package com.example.api_gateway.service;

import com.ecommerce.common.security.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

    private SecretKey signingKey;

    @PostConstruct
    public void init() {

        signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public Long extractUserId(String token) {

        Claims claims = Jwts
                .parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userId = claims.get("userId");

        if (userId == null) {
            return null;
        }

        return ((Number) userId).longValue();
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    public List<String> extractRoles(String token) {

        return extractAllClaims(token)
                .get(JwtConstants.ROLE, List.class);
    }

    public Date extractExpiration(String token) {

        return extractAllClaims(token)
                .getExpiration();
    }

    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    public boolean isTokenValid(String token) {

        try {

            return !isTokenExpired(token);

        } catch (Exception ex) {

            return false;
        }
    }
}