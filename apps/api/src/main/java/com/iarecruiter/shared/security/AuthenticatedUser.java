package com.iarecruiter.shared.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class AuthenticatedUser {
    private final UUID id;
    private final String email;
    private final UUID companyId;
    private final String role;
}
