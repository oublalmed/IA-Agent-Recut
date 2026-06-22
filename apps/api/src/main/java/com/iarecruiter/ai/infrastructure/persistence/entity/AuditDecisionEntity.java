package com.iarecruiter.ai.infrastructure.persistence.entity;

import com.iarecruiter.ai.domain.model.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "audit_decisions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditDecisionEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "application_id", nullable = false) private UUID applicationId;
    @Column(name = "ai_report_id") private UUID aiReportId;
    @Column(name = "ai_score", nullable = false) private double aiScore;
    @Column(name = "human_override", nullable = false) private boolean humanOverride;
    @Enumerated(EnumType.STRING) @Column(name = "final_status", nullable = false) private ApplicationStatus finalStatus;
    @Column(name = "override_justification", columnDefinition = "TEXT") private String overrideJustification;
    @Column(name = "decided_by", nullable = false) private UUID decidedBy;
    @CreationTimestamp @Column(name = "decided_at", nullable = false, updatable = false) private Instant decidedAt;
}
