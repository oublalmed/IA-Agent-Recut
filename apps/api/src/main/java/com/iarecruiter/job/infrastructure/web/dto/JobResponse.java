package com.iarecruiter.job.infrastructure.web.dto;

import com.iarecruiter.job.domain.model.JobStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobResponse(
        UUID id,
        UUID companyId,
        String title,
        String description,
        String location,
        String remotePolicy,
        String contractType,
        Integer experienceYearsMin,
        Integer experienceYearsMax,
        Integer salaryMin,
        Integer salaryMax,
        String salaryCurrency,
        JobStatus status,
        Instant aiAnalyzedAt,
        String aiAnalysisJson,
        List<RequiredSkillDto> requiredSkills,
        Instant createdAt,
        Instant updatedAt
) {
    public record RequiredSkillDto(UUID skillId, String skillName, BigDecimal weight, boolean mandatory) {}
}
