package com.iarecruiter.job.infrastructure.web.dto;

import com.iarecruiter.job.domain.model.JobStatus;
import jakarta.validation.constraints.Size;

public record UpdateJobRequest(
        @Size(max = 255) String title,
        String description,
        @Size(max = 255) String location,
        String remotePolicy,
        String contractType,
        Integer experienceYearsMin,
        Integer experienceYearsMax,
        Integer salaryMin,
        Integer salaryMax,
        JobStatus status
) {}
