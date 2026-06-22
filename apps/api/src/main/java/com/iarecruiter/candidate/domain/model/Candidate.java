package com.iarecruiter.candidate.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @With
public class Candidate {
    private final UUID id;
    private final UUID companyId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String linkedinUrl;
    private final String location;
    private final boolean erased;
    private final Instant erasedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
