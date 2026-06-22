package com.iarecruiter.ai.infrastructure.persistence.jpa;

import com.iarecruiter.ai.infrastructure.persistence.entity.AuditDecisionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaAuditDecisionRepository extends JpaRepository<AuditDecisionEntity, UUID> {
    List<AuditDecisionEntity> findByApplicationIdIn(List<UUID> applicationIds);
    List<AuditDecisionEntity> findByCompanyId(UUID companyId);

    @Query(value = """
            SELECT ad.id, ad.application_id, c.email AS candidate_email,
                   CONCAT(c.first_name, ' ', c.last_name) AS candidate_name,
                   j.title AS job_title, ad.final_status AS decision,
                   ad.override_justification AS justification,
                   ad.ai_score, ad.human_override, ad.decided_at, ad.decided_by
            FROM audit_decisions ad
            JOIN applications app ON app.id = ad.application_id
            JOIN candidates c ON c.id = app.candidate_id
            JOIN jobs j ON j.id = app.job_id
            WHERE ad.company_id = :companyId
            ORDER BY ad.decided_at DESC
            """,
            countQuery = "SELECT COUNT(*) FROM audit_decisions WHERE company_id = :companyId",
            nativeQuery = true)
    Page<Object[]> findDetailsByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);
}
