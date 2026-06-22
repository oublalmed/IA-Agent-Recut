package com.iarecruiter.company.infrastructure.web.dto;

import com.iarecruiter.auth.domain.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record TeamMemberResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        boolean active,
        Instant createdAt
) {}
