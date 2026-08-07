package com.example.auth_service.security.jwt;

import com.ecommerce.common.security.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtProperties properties;

    private final JwtKeyProvider keyProvider;

    /**
     * Extract username from JWT
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract roles from JWT
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractAllClaims(token)
                .get(JwtConstants.ROLE, List.class);
    }

    /**
     * Extract token type (ACCESS / REFRESH)
     */
    public JwtTokenType extractTokenType(String token) {

        String tokenType = extractAllClaims(token)
                .get(JwtConstants.TOKEN_TYPE, String.class);

        return JwtTokenType.valueOf(tokenType);
    }

    /**
     * Extract expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Check whether token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token against UserDetails
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Generic claim extractor
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {

        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    /**
     * Parse all claims
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(keyProvider.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}