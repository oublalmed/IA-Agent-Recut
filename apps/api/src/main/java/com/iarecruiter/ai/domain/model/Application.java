package com.iarecruiter.ai.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @With
public class Application {
    private final UUID id;
    private final UUID companyId;
    private final UUID jobId;
    private final UUID candidateId;
    private final UUID resumeId;
    private final ApplicationStatus status;
    private final Instant appliedAt;
    private final Instant updatedAt;
}
