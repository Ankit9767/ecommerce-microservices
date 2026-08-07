package com.example.auth_service.exception;

public class TokenReuseDetectedException extends RuntimeException {

    public TokenReuseDetectedException(String message) {
        super(message);
    }

}
