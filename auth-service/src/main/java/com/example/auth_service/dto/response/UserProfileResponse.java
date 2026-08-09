package com.example.auth_service.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {

    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;
}