package com.iarecruiter.ai.infrastructure.web.dto;

import com.iarecruiter.ai.domain.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record AuditDecisionRequest(
        @NotNull ApplicationStatus finalStatus,
        boolean humanOverride,
        String justification
) {}
