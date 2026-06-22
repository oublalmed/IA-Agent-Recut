package com.iarecruiter.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@With
public class User {
    private final UUID id;
    private final UUID companyId;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final UserRole role;
    private final boolean active;
    private final boolean emailVerified;
    private final Instant lastLoginAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
