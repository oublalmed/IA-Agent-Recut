package com.iarecruiter.company.infrastructure.web.dto;

import com.iarecruiter.auth.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteMemberRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull UserRole role,
        @NotBlank @Size(min = 8, max = 100) String temporaryPassword
) {}
