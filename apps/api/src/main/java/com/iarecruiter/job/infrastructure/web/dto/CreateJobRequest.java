package com.iarecruiter.job.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJobRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        @Size(max = 255) String location,
        String remotePolicy,
        String contractType,
        Integer experienceYearsMin,
        Integer experienceYearsMax,
        Integer salaryMin,
        Integer salaryMax,
        String salaryCurrency
) {}
