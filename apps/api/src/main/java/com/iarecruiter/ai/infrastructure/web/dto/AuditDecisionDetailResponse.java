package com.iarecruiter.ai.infrastructure.web.dto;

import com.iarecruiter.ai.domain.model.ApplicationStatus;

import java.time.Instant;
import java.util.UUID;

public record AuditDecisionDetailResponse(
        UUID id,
        UUID applicationId,
        String candidateEmail,
        String candidateName,
        String jobTitle,
        ApplicationStatus decision,
        String justification,
        double aiScore,
        boolean humanOverride,
        Instant decidedAt,
        UUID decidedBy
) {}
