package com.iarecruiter.report.infrastructure.web.dto;

public record DashboardResponse(
        long cvAnalyzed,
        long activeJobs,
        double avgMatchScore,
        double estimatedTimeSavedHours
) {}
