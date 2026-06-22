package com.iarecruiter.ai.infrastructure.persistence.adapter;

import com.iarecruiter.ai.domain.model.AiReport;
import com.iarecruiter.ai.domain.port.AiReportRepository;
import com.iarecruiter.ai.infrastructure.persistence.entity.AiReportEntity;
import com.iarecruiter.ai.infrastructure.persistence.jpa.JpaAiReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AiReportRepositoryAdapter implements AiReportRepository {
    private final JpaAiReportRepository jpa;

    @Override
    public AiReport save(AiReport r) { return toDomain(jpa.save(toEntity(r))); }

    @Override
    public Optional<AiReport> findByApplicationId(UUID id) { return jpa.findByApplicationId(id).map(this::toDomain); }

    private AiReport toDomain(AiReportEntity e) {
        return AiReport.builder().id(e.getId()).companyId(e.getCompanyId())
                .applicationId(e.getApplicationId()).matchScore(e.getMatchScore())
                .skillScore(e.getSkillScore()).experienceScore(e.getExperienceScore())
                .educationScore(e.getEducationScore()).languageScore(e.getLanguageScore())
                .strengths(e.getStrengths()).weaknesses(e.getWeaknesses())
                .recommendation(e.getRecommendation()).rawLlmResponse(e.getRawLlmResponse())
                .modelUsed(e.getModelUsed()).tokensInput(e.getTokensInput()).tokensOutput(e.getTokensOutput())
                .generatedAt(e.getGeneratedAt()).build();
    }
    private AiReportEntity toEntity(AiReport r) {
        return AiReportEntity.builder().id(r.getId()).companyId(r.getCompanyId())
                .applicationId(r.getApplicationId()).matchScore(r.getMatchScore())
                .skillScore(r.getSkillScore()).experienceScore(r.getExperienceScore())
                .educationScore(r.getEducationScore()).languageScore(r.getLanguageScore())
                .strengths(r.getStrengths()).weaknesses(r.getWeaknesses())
                .recommendation(r.getRecommendation()).rawLlmResponse(r.getRawLlmResponse())
                .modelUsed(r.getModelUsed()).tokensInput(r.getTokensInput()).tokensOutput(r.getTokensOutput()).build();
    }
}
