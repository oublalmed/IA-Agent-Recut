package com.iarecruiter.ai.infrastructure.persistence.adapter;

import com.iarecruiter.ai.domain.model.AuditDecision;
import com.iarecruiter.ai.domain.port.AuditDecisionRepository;
import com.iarecruiter.ai.infrastructure.persistence.entity.AuditDecisionEntity;
import com.iarecruiter.ai.infrastructure.persistence.jpa.JpaAuditDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AuditDecisionRepositoryAdapter implements AuditDecisionRepository {
    private final JpaAuditDecisionRepository jpa;

    @Override public AuditDecision save(AuditDecision d) { return toDomain(jpa.save(toEntity(d))); }
    @Override public List<AuditDecision> findByJobId(UUID jobId) {
        // findByJobId requires a join; deferred to S6 — returns empty for now
        return List.of();
    }
    @Override public List<AuditDecision> findByCompanyId(UUID companyId) {
        return jpa.findByCompanyId(companyId).stream().map(this::toDomain).toList();
    }

    private AuditDecision toDomain(AuditDecisionEntity e) {
        return AuditDecision.builder().id(e.getId()).companyId(e.getCompanyId())
                .applicationId(e.getApplicationId()).aiReportId(e.getAiReportId())
                .aiScore(e.getAiScore()).humanOverride(e.isHumanOverride())
                .finalStatus(e.getFinalStatus()).overrideJustification(e.getOverrideJustification())
                .decidedBy(e.getDecidedBy()).decidedAt(e.getDecidedAt()).build();
    }
    private AuditDecisionEntity toEntity(AuditDecision d) {
        return AuditDecisionEntity.builder().id(d.getId()).companyId(d.getCompanyId())
                .applicationId(d.getApplicationId()).aiReportId(d.getAiReportId())
                .aiScore(d.getAiScore()).humanOverride(d.isHumanOverride())
                .finalStatus(d.getFinalStatus()).overrideJustification(d.getOverrideJustification())
                .decidedBy(d.getDecidedBy()).build();
    }
}
