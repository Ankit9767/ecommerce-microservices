package com.example.auth_service.service;

import com.example.auth_service.dto.request.UpdateProfileRequest;
import com.example.auth_service.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentUser();

    UserProfileResponse updateCurrentUser(UpdateProfileRequest request);
}