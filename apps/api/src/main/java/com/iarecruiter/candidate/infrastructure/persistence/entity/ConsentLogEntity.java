package com.iarecruiter.candidate.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "consent_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsentLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "consent_type", nullable = false) private String consentType;
    @Column(nullable = false) private boolean granted;
    @Column(name = "ip_address") private String ipAddress;

    @CreationTimestamp @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;
}
