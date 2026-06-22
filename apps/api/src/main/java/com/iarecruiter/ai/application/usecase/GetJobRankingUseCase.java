package com.iarecruiter.ai.application.usecase;

import com.iarecruiter.ai.domain.model.*;
import com.iarecruiter.ai.domain.port.*;
import com.iarecruiter.candidate.domain.port.CandidateRepository;
import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.candidate.domain.port.ResumeStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetJobRankingUseCase {

    private final ApplicationRepository applicationRepository;
    private final AiReportRepository aiReportRepository;
    private final AuditDecisionRepository auditDecisionRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeStoragePort resumeStoragePort;

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

                    String cvUrl = null;
                    try {
                        var latestResume = resumeRepository.findLatestByCandidateId(app.getCandidateId());
                        if (latestResume.isPresent()) {
                            cvUrl = resumeStoragePort.getPresignedUrl(latestResume.get().getMinioObjectKey(), 3600);
                        }
                    } catch (Exception e) {
                        log.warn("Could not generate presigned URL for candidate {}: {}", app.getCandidateId(), e.getMessage());
                    }

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
                            .cvUrl(cvUrl)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(CandidateRanking::getMatchScore).reversed())
                .collect(Collectors.toList());
    }
}
