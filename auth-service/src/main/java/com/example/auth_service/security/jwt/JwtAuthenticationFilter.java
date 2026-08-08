package com.example.auth_service.security.jwt;

import com.example.auth_service.security.CustomUserDetailsService;
import com.example.auth_service.service.UserSessionService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;

    private final CustomUserDetailsService userDetailsService;

    private final UserSessionService userSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        /*
         * No Bearer token.
         *
         * Let Spring Security decide whether
         * the endpoint is public or protected.
         */
        if (header == null ||
                !header.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );
            return;
        }

        String token = header.substring(7);

        try {

            String username = jwtTokenValidator.extractUsername(token);

            if (username != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {


                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        username
                                );


                if (jwtTokenValidator.isTokenValid(token, userDetails)) {

                    /*
                     * Authenticate the request.
                     */
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );


                    /*
                     * Extract the session ID from
                     * the access JWT.
                     */
                    String sessionId = jwtTokenValidator.extractSessionId(token);


                    /*
                     * Update last activity of the
                     * corresponding user session.
                     */
                    if (sessionId != null) {

                        userSessionService
                                .updateLastActivity(
                                        sessionId
                                );
                    }
                }
            }

        } catch (JwtException ex) {

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    """
                    {
                        "error": "Invalid JWT token"
                    }
                    """
            );

            return;

        }

        filterChain.doFilter(
                request,
                response
        );
    }
}