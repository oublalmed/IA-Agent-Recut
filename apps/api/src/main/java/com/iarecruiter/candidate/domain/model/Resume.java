package com.iarecruiter.candidate.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @With
public class Resume {
    private final UUID id;
    private final UUID companyId;
    private final UUID candidateId;
    private final UUID uploadedBy;
    private final String originalFilename;
    private final String minioObjectKey;
    private final String mimeType;
    private final Long fileSizeBytes;
    private final ResumeStatus status;
    private final String extractedData;   // JSON string
    private final String extractionError;
    private final Instant processedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
