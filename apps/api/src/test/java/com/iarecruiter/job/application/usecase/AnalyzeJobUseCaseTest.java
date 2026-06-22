package com.iarecruiter.job.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.model.JobAnalysis;
import com.iarecruiter.job.domain.model.JobStatus;
import com.iarecruiter.job.domain.port.AiJobAnalysisPort;
import com.iarecruiter.job.domain.port.JobRepository;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyzeJobUseCaseTest {

    @Mock private JobRepository jobRepository;
    @Mock private AiJobAnalysisPort aiJobAnalysisPort;
    @InjectMocks private AnalyzeJobUseCase analyzeJobUseCase;

    private UUID companyId;
    private UUID jobId;
    private Job existingJob;

    @BeforeEach
    void setUp() throws Exception {
        var objectMapper = new ObjectMapper();
        var field = AnalyzeJobUseCase.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(analyzeJobUseCase, objectMapper);

        companyId = UUID.randomUUID();
        jobId = UUID.randomUUID();
        existingJob = Job.builder()
                .id(jobId)
                .companyId(companyId)
                .createdBy(UUID.randomUUID())
                .title("Senior Java Developer")
                .description("We are looking for a senior Java developer...")
                .status(JobStatus.DRAFT)
                .requiredSkills(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void execute_savesAnalysisResult() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(existingJob));
        when(aiJobAnalysisPort.analyzeJob(anyString(), anyString())).thenReturn(
                JobAnalysis.builder()
                        .jobTitle("Senior Java Developer")
                        .summary("A senior backend role")
                        .requiredSkills(List.of(JobAnalysis.SkillRequirement.builder()
                                .name("Java").category("TECHNICAL").weight(1.0).mandatory(true).build()))
                        .niceToHaveSkills(List.of())
                        .minExperienceYears(5)
                        .educationRequirements(List.of())
                        .languages(List.of("French"))
                        .build()
        );
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Job result = analyzeJobUseCase.execute(jobId, companyId);

        assertThat(result.getAiAnalysisJson()).isNotBlank();
        assertThat(result.getAiAnalyzedAt()).isNotNull();
        verify(aiJobAnalysisPort).analyzeJob("Senior Java Developer", existingJob.getDescription());
    }

    @Test
    void execute_throwsWhenJobNotFound() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyzeJobUseCase.execute(jobId, companyId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_throwsWhenDifferentCompany() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(existingJob));

        assertThatThrownBy(() -> analyzeJobUseCase.execute(jobId, UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Access denied");
    }
}
