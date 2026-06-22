package com.iarecruiter.ai.domain.port;

import com.iarecruiter.ai.domain.model.AiReport;

public interface AiMatchingPort {
    AiReport generateAnalysis(MatchingContext context);

    record MatchingContext(
            String jobTitle,
            String jobDescription,
            String jobAnalysisJson,
            String resumeExtractedJson,
            double skillScore,
            double experienceScore,
            double educationScore,
            double languageScore,
            double overallScore
    ) {}
}
