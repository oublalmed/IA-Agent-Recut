package com.iarecruiter.ai.application.usecase;

import com.iarecruiter.ai.application.service.ScoringService;
import com.iarecruiter.ai.domain.model.AiReport;
import com.iarecruiter.ai.domain.model.Application;
import com.iarecruiter.ai.domain.port.AiMatchingPort;
import com.iarecruiter.ai.domain.port.AiReportRepository;
import com.iarecruiter.ai.domain.port.ApplicationRepository;
import com.iarecruiter.candidate.domain.model.Resume;
import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.port.JobRepository;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchCandidateUseCaseTest {

    @Mock private JobRepository jobRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private AiReportRepository aiReportRepository;
    @Mock private ScoringService scoringService;
    @Mock private AiMatchingPort aiMatchingPort;

    @InjectMocks
    private MatchCandidateUseCase matchCandidateUseCase;

    private final UUID companyId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();
    private final UUID resumeId = UUID.randomUUID();
    private final UUID candidateId = UUID.randomUUID();

    @Test
    void execute_jobNotFound_throwsResourceNotFoundException() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchCandidateUseCase.execute(jobId, resumeId, companyId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(resumeRepository, never()).findById(any());
    }

    @Test
    void execute_resumeNotFound_throwsResourceNotFoundException() {
        Job job = buildJob(companyId, "{}", "{}");
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchCandidateUseCase.execute(jobId, resumeId, companyId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_jobHasNoAiAnalysis_throwsBusinessException() {
        Job job = buildJob(companyId, null, null);
        Resume resume = buildResume(companyId, candidateId, "{\"skills\":[]}");
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        assertThatThrownBy(() -> matchCandidateUseCase.execute(jobId, resumeId, companyId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("analyzed");
    }

    @Test
    void execute_successfulMatch_savesApplicationAndReport() {
        Job job = buildJob(companyId, "{}", "{\"requiredSkills\":[]}");
        Resume resume = buildResume(companyId, candidateId, "{\"skills\":[]}");
        Application application = Application.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .jobId(jobId)
                .candidateId(candidateId)
                .resumeId(resumeId)
                .build();
        AiReport aiReport = AiReport.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .applicationId(application.getId())
                .matchScore(75.0)
                .skillScore(80.0)
                .experienceScore(70.0)
                .educationScore(60.0)
                .languageScore(90.0)
                .strengths(List.of("Java"))
                .weaknesses(List.of())
                .recommendation("Strong candidate")
                .modelUsed("claude-haiku-4-5-20251001")
                .generatedAt(Instant.now())
                .build();
        ScoringService.ScoreResult scoreResult = new ScoringService.ScoreResult(75.0, 80.0, 70.0, 60.0, 90.0); // overall, skill, exp, edu, lang

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));
        when(applicationRepository.findByJobIdAndCandidateId(jobId, candidateId))
                .thenReturn(Optional.of(application));
        when(scoringService.score(any(), any())).thenReturn(scoreResult);
        when(aiMatchingPort.generateAnalysis(any())).thenReturn(aiReport);
        when(aiReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        matchCandidateUseCase.execute(jobId, resumeId, companyId);

        verify(aiReportRepository).save(any(AiReport.class));
        verify(applicationRepository, never()).save(any()); // application already existed
    }

    // ---- helpers ----

    private Job buildJob(UUID companyId, String extractedData, String aiAnalysisJson) {
        return Job.builder()
                .id(jobId)
                .companyId(companyId)
                .title("Software Engineer")
                .description("Build great things")
                .aiAnalysisJson(aiAnalysisJson)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Resume buildResume(UUID companyId, UUID candidateId, String extractedData) {
        return Resume.builder()
                .id(resumeId)
                .companyId(companyId)
                .candidateId(candidateId)
                .uploadedBy(UUID.randomUUID())
                .originalFilename("cv.pdf")
                .extractedData(extractedData)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
