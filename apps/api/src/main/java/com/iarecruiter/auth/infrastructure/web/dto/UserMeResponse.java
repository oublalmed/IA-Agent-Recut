package com.iarecruiter.auth.infrastructure.web.dto;

import com.iarecruiter.auth.domain.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserMeResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        UUID companyId,
        boolean emailVerified,
        Instant createdAt
) {}
