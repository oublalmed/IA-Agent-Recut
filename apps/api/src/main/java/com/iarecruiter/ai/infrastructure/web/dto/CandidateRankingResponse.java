package com.iarecruiter.ai.infrastructure.web.dto;

import com.iarecruiter.ai.domain.model.ApplicationStatus;
import java.util.List;
import java.util.UUID;

public record CandidateRankingResponse(
        UUID applicationId,
        UUID candidateId,
        String candidateEmail,
        String candidateFirstName,
        String candidateLastName,
        double matchScore,
        double skillScore,
        double experienceScore,
        List<String> strengths,
        List<String> weaknesses,
        String recommendation,
        ApplicationStatus status,
        boolean humanOverride
) {}
