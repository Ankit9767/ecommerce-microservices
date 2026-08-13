package com.example.auth_service.security.jwt;

import com.ecommerce.common.security.JwtClaimsExtractor;
import com.ecommerce.common.security.JwtConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtKeyProvider keyProvider;

    private JwtClaimsExtractor claimsExtractor() {
        return new JwtClaimsExtractor(
                keyProvider.getSigningKey()
        );
    }

    public String extractUsername(String token) {

        return claimsExtractor()
                .extractUsername(token);
    }

    public List<String> extractRoles(String token) {

        return claimsExtractor()
                .extractRoles(token);
    }

    public JwtTokenType extractTokenType(String token) {

        String tokenType =
                claimsExtractor()
                        .extractAllClaims(token)
                        .get(
                                JwtConstants.TOKEN_TYPE,
                                String.class
                        );

        return JwtTokenType.valueOf(tokenType);
    }

    public Date extractExpiration(String token) {

        return claimsExtractor()
                .extractExpiration(token);
    }

    public boolean isTokenExpired(String token) {

        return claimsExtractor()
                .isTokenExpired(token);
    }

    public boolean isTokenValid(String token,
            UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public String extractSessionId(String token) {

        return claimsExtractor()
                .extractAllClaims(token)
                .get(
                        JwtConstants.SESSION_ID,
                        String.class
                );
    }

    public Long extractUserId(String token) {

        return claimsExtractor()
                .extractUserId(token);
    }
}