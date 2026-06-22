package com.iarecruiter.ai.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter @Builder
public class AiReport {
    private final UUID id;
    private final UUID companyId;
    private final UUID applicationId;
    private final double matchScore;
    private final double skillScore;
    private final double experienceScore;
    private final double educationScore;
    private final double languageScore;
    private final List<String> strengths;
    private final List<String> weaknesses;
    private final String recommendation;
    private final String rawLlmResponse;
    private final String modelUsed;
    private final Integer tokensInput;
    private final Integer tokensOutput;
    private final Instant generatedAt;
}
