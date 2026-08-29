package com.ecommerce.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

public class JwtClaimsExtractor {

    private final SecretKey signingKey;

    public JwtClaimsExtractor(SecretKey signingKey) {
        this.signingKey = signingKey;
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

    public Long extractUserId(String token) {

        Object userId =
                extractAllClaims(token)
                        .get(JwtConstants.USER_ID);

        if (userId == null) {
            return null;
        }

        return ((Number) userId).longValue();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token)
                .get(JwtConstants.EMAIL, String.class);
    }

    public List<String> extractRoles(String token) {

        return extractAllClaims(token)
                .get(JwtConstants.ROLE, List.class);
    }

    public String extractSessionId(String token) {

        return extractAllClaims(token)
                .get(
                        JwtConstants.SESSION_ID,
                        String.class
                );
    }

    public String extractTokenType(String token) {

        return extractAllClaims(token)
                .get(
                        JwtConstants.TOKEN_TYPE,
                        String.class
                );
    }

    public Date extractExpiration(String token) {

        return extractAllClaims(token)
                .getExpiration();
    }

    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }
}