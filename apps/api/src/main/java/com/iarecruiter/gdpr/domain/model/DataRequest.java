package com.iarecruiter.gdpr.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DataRequest {
    private UUID id;
    private UUID companyId;
    private UUID candidateId;
    private DataRequestType requestType;
    private DataRequestStatus status;
    private Instant requestedAt;
    private Instant completedAt;
    private String notes;
}
