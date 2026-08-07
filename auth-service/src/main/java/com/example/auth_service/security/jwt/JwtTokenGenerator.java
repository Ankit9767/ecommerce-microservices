package com.example.auth_service.security.jwt;

import com.ecommerce.common.security.JwtConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenGenerator {

    private final JwtProperties properties;

    private final JwtKeyProvider keyProvider;

    public String generateAccessToken(UserDetails userDetails) {

        List<String> roles =
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(JwtConstants.ROLE, roles)
                .claim(JwtConstants.TOKEN_TYPE, JwtTokenType.ACCESS.name())
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + properties.getExpiration()
                ))
                .signWith(keyProvider.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}