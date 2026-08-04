package com.example.auth_service.Dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private Long userId;

    private String username;

    private String email;

    private String message;
}
