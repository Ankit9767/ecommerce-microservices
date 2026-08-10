package com.example.auth_service.security;

import com.example.auth_service.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        ApiError error =
                ApiError.builder()
                        .timestamp(LocalDateTime.from(Instant.now()))
                        .status(401)
                        .error("Unauthorized")
                        .message("Authentication required")
                        .path(request.getRequestURI())
                        .build();

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}