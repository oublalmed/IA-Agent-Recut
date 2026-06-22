package com.iarecruiter.job.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.model.JobAnalysis;
import com.iarecruiter.job.domain.port.AiJobAnalysisPort;
import com.iarecruiter.job.domain.port.JobRepository;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyzeJobUseCase {

    private final JobRepository jobRepository;
    private final AiJobAnalysisPort aiJobAnalysisPort;
    private final ObjectMapper objectMapper;

    @Transactional
    public Job execute(UUID jobId, UUID companyId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId.toString()));

        if (!job.getCompanyId().equals(companyId)) {
            throw new BusinessException("Access denied to this job");
        }

        JobAnalysis analysis = aiJobAnalysisPort.analyzeJob(job.getTitle(), job.getDescription());

        String analysisJson;
        try {
            analysisJson = objectMapper.writeValueAsString(analysis);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to serialize AI analysis result");
        }

        Job updated = job
                .withAiAnalysisJson(analysisJson)
                .withAiAnalyzedAt(Instant.now())
                .withUpdatedAt(Instant.now());

        return jobRepository.save(updated);
    }
}
