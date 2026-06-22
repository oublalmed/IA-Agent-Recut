package com.iarecruiter.ai.application.usecase;

import com.iarecruiter.ai.domain.model.*;
import com.iarecruiter.ai.domain.port.*;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordAuditDecisionUseCase {

    private final ApplicationRepository applicationRepository;
    private final AiReportRepository aiReportRepository;
    private final AuditDecisionRepository auditDecisionRepository;

    @Transactional
    public AuditDecision execute(UUID applicationId, UUID decidedBy, UUID companyId,
                                  ApplicationStatus finalStatus, boolean humanOverride,
                                  String justification) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId.toString()));

        AiReport report = aiReportRepository.findByApplicationId(applicationId).orElse(null);
        double aiScore = report != null ? report.getMatchScore() : 0.0;

        applicationRepository.save(application.withStatus(finalStatus).withUpdatedAt(Instant.now()));

        return auditDecisionRepository.save(AuditDecision.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .applicationId(applicationId)
                .aiReportId(report != null ? report.getId() : null)
                .aiScore(aiScore)
                .humanOverride(humanOverride)
                .finalStatus(finalStatus)
                .overrideJustification(justification)
                .decidedBy(decidedBy)
                .decidedAt(Instant.now())
                .build());
    }
}
