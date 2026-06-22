package com.iarecruiter.candidate.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iarecruiter.candidate.domain.model.ExtractedResumeData;
import com.iarecruiter.candidate.domain.model.Resume;
import com.iarecruiter.candidate.domain.model.ResumeStatus;
import com.iarecruiter.candidate.domain.port.AiResumeExtractionPort;
import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.candidate.domain.port.ResumeStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractResumeUseCase {

    private final ResumeRepository resumeRepository;
    private final ResumeStoragePort resumeStoragePort;
    private final AiResumeExtractionPort aiResumeExtractionPort;
    private final ObjectMapper objectMapper;
    private final Tika tika = new Tika();

    @Transactional
    public Resume execute(UUID resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + resumeId));

        // Mark as processing
        resumeRepository.save(resume.withStatus(ResumeStatus.PROCESSING).withUpdatedAt(Instant.now()));

        try {
            // Download from MinIO and extract text with Tika
            InputStream fileStream = resumeStoragePort.download(resume.getMinioObjectKey());
            String rawText = tika.parseToString(fileStream);

            if (rawText == null || rawText.isBlank()) {
                throw new IllegalStateException("Could not extract text from document");
            }

            // AI extraction
            ExtractedResumeData extracted = aiResumeExtractionPort.extractFromText(rawText);
            String extractedJson = objectMapper.writeValueAsString(extracted);

            Resume updated = resume
                    .withStatus(ResumeStatus.EXTRACTED)
                    .withExtractedData(extractedJson)
                    .withProcessedAt(Instant.now())
                    .withUpdatedAt(Instant.now());

            return resumeRepository.save(updated);

        } catch (Exception e) {
            log.error("Failed to extract resume {}: {}", resumeId, e.getMessage());
            Resume failed = resume
                    .withStatus(ResumeStatus.FAILED)
                    .withExtractionError(e.getMessage())
                    .withUpdatedAt(Instant.now());
            resumeRepository.save(failed);
            throw new RuntimeException("Resume extraction failed: " + e.getMessage(), e);
        }
    }
}
