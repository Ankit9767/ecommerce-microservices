package com.example.auth_service.security.refresh;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class TokenHashService {

    public String hash(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder builder = new StringBuilder();

            for (byte b : hash) {

                builder.append(
                        String.format("%02x", b)
                );

            }

            return builder.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(e);

        }

    }

}