package com.iarecruiter.company.domain.model;

import com.iarecruiter.auth.domain.model.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class TeamMember {
    private final UUID id;
    private final UUID companyId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final UserRole role;
    private final boolean active;
    private final Instant createdAt;
}
