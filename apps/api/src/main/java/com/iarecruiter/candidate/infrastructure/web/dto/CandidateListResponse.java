package com.iarecruiter.candidate.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record CandidateListResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String currentTitle,
        Instant createdAt,
        long resumeCount
) {}
