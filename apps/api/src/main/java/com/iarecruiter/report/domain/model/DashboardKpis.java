package com.iarecruiter.report.domain.model;

public record DashboardKpis(
        long cvAnalyzed,
        long activeJobs,
        double avgMatchScore,
        double estimatedTimeSavedHours
) {}
