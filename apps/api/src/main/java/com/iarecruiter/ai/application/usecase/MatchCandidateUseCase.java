package com.iarecruiter.ai.application.usecase;

import com.iarecruiter.ai.application.service.ScoringService;
import com.iarecruiter.ai.domain.model.*;
import com.iarecruiter.ai.domain.port.*;
import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.job.domain.port.JobRepository;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchCandidateUseCase {

    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final AiReportRepository aiReportRepository;
    private final AiMatchingPort aiMatchingPort;
    private final ScoringService scoringService;

    @Transactional
    public AiReport execute(UUID jobId, UUID resumeId, UUID companyId) {
        var job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId.toString()));
        var resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId.toString()));

        if (!job.getCompanyId().equals(companyId) || !resume.getCompanyId().equals(companyId)) {
            throw new BusinessException("Access denied");
        }
        if (resume.getExtractedData() == null) {
            throw new BusinessException("Resume has not been extracted yet. Wait for status EXTRACTED.");
        }
        if (job.getAiAnalysisJson() == null) {
            throw new BusinessException("Job has not been analyzed yet. Call POST /api/jobs/{id}/analyze first.");
        }

        Application application = applicationRepository
                .findByJobIdAndCandidateId(jobId, resume.getCandidateId())
                .orElseGet(() -> applicationRepository.save(Application.builder()
                        .id(UUID.randomUUID())
                        .companyId(companyId)
                        .jobId(jobId)
                        .candidateId(resume.getCandidateId())
                        .resumeId(resumeId)
                        .status(ApplicationStatus.PENDING)
                        .appliedAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()));

        ScoringService.ScoreResult scores = scoringService.score(
                job.getAiAnalysisJson(), resume.getExtractedData());

        AiReport report = aiMatchingPort.generateAnalysis(new AiMatchingPort.MatchingContext(
                job.getTitle(), job.getDescription(), job.getAiAnalysisJson(),
                resume.getExtractedData(),
                scores.skillScore(), scores.experienceScore(),
                scores.educationScore(), scores.languageScore(),
                scores.overall()
        ));

        AiReport saved = aiReportRepository.save(AiReport.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .applicationId(application.getId())
                .matchScore(report.getMatchScore())
                .skillScore(scores.skillScore())
                .experienceScore(scores.experienceScore())
                .educationScore(scores.educationScore())
                .languageScore(scores.languageScore())
                .strengths(report.getStrengths())
                .weaknesses(report.getWeaknesses())
                .recommendation(report.getRecommendation())
                .rawLlmResponse(report.getRawLlmResponse())
                .modelUsed(report.getModelUsed())
                .tokensInput(report.getTokensInput())
                .tokensOutput(report.getTokensOutput())
                .generatedAt(Instant.now())
                .build());

        return saved;
    }
}
