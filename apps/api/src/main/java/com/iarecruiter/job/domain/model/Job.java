package com.iarecruiter.job.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@With
public class Job {
    private final UUID id;
    private final UUID companyId;
    private final UUID createdBy;
    private final String title;
    private final String description;
    private final String location;
    private final String remotePolicy;
    private final String contractType;
    private final Integer experienceYearsMin;
    private final Integer experienceYearsMax;
    private final Integer salaryMin;
    private final Integer salaryMax;
    private final String salaryCurrency;
    private final JobStatus status;
    private final Instant aiAnalyzedAt;
    private final String aiAnalysisJson;
    private final List<RequiredSkill> requiredSkills;
    private final Instant createdAt;
    private final Instant updatedAt;
}
