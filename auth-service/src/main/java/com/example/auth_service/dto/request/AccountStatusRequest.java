package com.example.auth_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountStatusRequest {

    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}