package com.iarecruiter.auth.infrastructure.web.dto;

import com.iarecruiter.auth.domain.model.UserRole;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
    public record UserResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            UserRole role,
            UUID companyId
    ) {}
}
