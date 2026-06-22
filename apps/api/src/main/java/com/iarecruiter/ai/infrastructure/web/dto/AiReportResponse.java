package com.iarecruiter.ai.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiReportResponse(
        UUID id,
        UUID applicationId,
        double matchScore,
        double skillScore,
        double experienceScore,
        double educationScore,
        double languageScore,
        List<String> strengths,
        List<String> weaknesses,
        String recommendation,
        String modelUsed,
        Instant generatedAt
) {}
