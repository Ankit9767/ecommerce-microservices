package com.example.auth_service.security.jwt;

import com.ecommerce.common.security.JwtConstants;
import com.example.auth_service.security.CustomUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtTokenGenerator {

    private final JwtProperties properties;

    private final JwtKeyProvider keyProvider;

    public String generateAccessToken(UserDetails userDetails, String sessionId) {

        Map<String, Object> claims = new HashMap<>();

        List<String> roles =
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        var user = customUserDetails.getUser();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(claims)
                .claim(JwtConstants.SESSION_ID, sessionId)
                .claim(JwtConstants.USER_ID, customUserDetails.getUser().getId())
                .claim( JwtConstants.EMAIL, user.getEmail())
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