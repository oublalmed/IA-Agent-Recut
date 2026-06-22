package com.iarecruiter.report.infrastructure.persistence;

import com.iarecruiter.report.domain.model.DashboardKpis;
import com.iarecruiter.report.domain.port.DashboardPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DashboardRepositoryAdapter implements DashboardPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public DashboardKpis getDashboardKpis(UUID companyId) {
        // CVs analyzed: status not UPLOADED (PENDING) and not FAILED
        Long cvAnalyzed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM resumes WHERE company_id = ? AND status IN ('PROCESSING', 'EXTRACTED')",
                Long.class, companyId);

        // Active jobs: PUBLISHED or PAUSED (not DRAFT or CLOSED)
        Long activeJobs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM jobs WHERE company_id = ? AND status IN ('PUBLISHED', 'PAUSED')",
                Long.class, companyId);

        // Avg match score: from applications with an ai_report, status != REJECTED
        Double avgScore = jdbcTemplate.queryForObject(
                "SELECT AVG(ar.match_score) FROM ai_reports ar " +
                "JOIN applications a ON ar.application_id = a.id " +
                "WHERE a.company_id = ? AND a.status != 'REJECTED'",
                Double.class, companyId);

        long cv = cvAnalyzed != null ? cvAnalyzed : 0L;
        long jobs = activeJobs != null ? activeJobs : 0L;
        double avg = avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0;
        double timeSaved = cv * 0.5;

        return new DashboardKpis(cv, jobs, avg, timeSaved);
    }
}
