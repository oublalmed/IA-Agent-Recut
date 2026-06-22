package com.iarecruiter.ai.application.usecase;

import com.iarecruiter.ai.domain.model.*;
import com.iarecruiter.ai.domain.port.*;
import com.iarecruiter.candidate.domain.port.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetJobRankingUseCase {

    private final ApplicationRepository applicationRepository;
    private final AiReportRepository aiReportRepository;
    private final AuditDecisionRepository auditDecisionRepository;
    private final CandidateRepository candidateRepository;

    public List<CandidateRanking> execute(UUID jobId, Double minScore, List<String> skills) {
        List<Application> applications = applicationRepository.findByJobId(jobId);

        List<UUID> applicationIds = applications.stream().map(Application::getId).toList();

        return applications.stream()
                .map(app -> {
                    Optional<AiReport> report = aiReportRepository.findByApplicationId(app.getId());
                    if (report.isEmpty()) return null;

                    AiReport r = report.get();
                    if (minScore != null && r.getMatchScore() < minScore) return null;

                    boolean humanOverride = auditDecisionRepository.findByCompanyId(app.getCompanyId()).stream()
                            .filter(ad -> ad.getApplicationId().equals(app.getId()))
                            .max(Comparator.comparing(AuditDecision::getDecidedAt))
                            .map(AuditDecision::isHumanOverride)
                            .orElse(false);

                    var candidate = candidateRepository.findById(app.getCandidateId()).orElse(null);

                    return CandidateRanking.builder()
                            .applicationId(app.getId())
                            .candidateId(app.getCandidateId())
                            .resumeId(app.getResumeId())
                            .candidateEmail(candidate != null ? candidate.getEmail() : null)
                            .candidateFirstName(candidate != null ? candidate.getFirstName() : null)
                            .candidateLastName(candidate != null ? candidate.getLastName() : null)
                            .matchScore(r.getMatchScore())
                            .skillScore(r.getSkillScore())
                            .experienceScore(r.getExperienceScore())
                            .strengths(r.getStrengths())
                            .weaknesses(r.getWeaknesses())
                            .recommendation(r.getRecommendation())
                            .status(app.getStatus())
                            .humanOverride(humanOverride)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(CandidateRanking::getMatchScore).reversed())
                .collect(Collectors.toList());
    }
}
