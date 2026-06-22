package com.iarecruiter.ai.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder
public class AuditDecision {
    private final UUID id;
    private final UUID companyId;
    private final UUID applicationId;
    private final UUID aiReportId;
    private final double aiScore;
    private final boolean humanOverride;
    private final ApplicationStatus finalStatus;
    private final String overrideJustification;
    private final UUID decidedBy;
    private final Instant decidedAt;
}
