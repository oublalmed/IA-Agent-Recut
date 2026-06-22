package com.iarecruiter.ai.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter @Builder
public class CandidateRanking {
    private final UUID applicationId;
    private final UUID candidateId;
    private final UUID resumeId;
    private final String candidateEmail;
    private final String candidateFirstName;
    private final String candidateLastName;
    private final double matchScore;
    private final double skillScore;
    private final double experienceScore;
    private final List<String> strengths;
    private final List<String> weaknesses;
    private final String recommendation;
    private final ApplicationStatus status;
    private final boolean humanOverride;
}
