package com.iarecruiter.candidate.infrastructure.persistence.entity;

import com.iarecruiter.candidate.domain.model.ResumeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "resumes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResumeEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Column(name = "uploaded_by", nullable = false) private UUID uploadedBy;
    @Column(name = "original_filename", nullable = false) private String originalFilename;
    @Column(name = "minio_object_key", nullable = false, unique = true) private String minioObjectKey;
    @Column(name = "mime_type") private String mimeType;
    @Column(name = "file_size_bytes") private Long fileSizeBytes;

    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private ResumeStatus status = ResumeStatus.UPLOADED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_data", columnDefinition = "jsonb")
    private String extractedData;

    @Column(name = "extraction_error", columnDefinition = "TEXT")
    private String extractionError;

    @Column(name = "processed_at") private Instant processedAt;

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
