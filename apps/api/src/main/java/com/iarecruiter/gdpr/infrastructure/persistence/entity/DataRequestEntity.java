package com.iarecruiter.gdpr.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "data_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DataRequestEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "request_type", nullable = false)
    private String requestType;

    @Column(name = "status", nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
