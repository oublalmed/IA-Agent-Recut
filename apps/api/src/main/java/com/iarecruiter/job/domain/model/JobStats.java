package com.iarecruiter.job.domain.model;

public record JobStats(
        long totalApplications,
        double avgMatchScore,
        int topScore,
        long pendingReview
) {}
