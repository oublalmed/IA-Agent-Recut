package com.iarecruiter.ai.infrastructure.persistence.entity;

import com.iarecruiter.ai.domain.model.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApplicationEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "job_id", nullable = false) private UUID jobId;
    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Column(name = "resume_id", nullable = false) private UUID resumeId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;
    @CreationTimestamp @Column(name = "applied_at", nullable = false, updatable = false) private Instant appliedAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
