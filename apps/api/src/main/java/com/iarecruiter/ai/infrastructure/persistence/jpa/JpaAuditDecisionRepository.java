package com.iarecruiter.ai.infrastructure.persistence.jpa;

import com.iarecruiter.ai.infrastructure.persistence.entity.AuditDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaAuditDecisionRepository extends JpaRepository<AuditDecisionEntity, UUID> {
    List<AuditDecisionEntity> findByApplicationIdIn(List<UUID> applicationIds);
    List<AuditDecisionEntity> findByCompanyId(UUID companyId);
}
