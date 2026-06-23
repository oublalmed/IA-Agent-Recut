package com.iarecruiter.job.infrastructure.persistence.adapter;

import com.iarecruiter.job.domain.model.JobStats;
import com.iarecruiter.job.domain.port.JobStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JobStatsRepositoryAdapter implements JobStatsRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public JobStats getStatsForJob(UUID jobId) {
        Long totalApplications = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM applications WHERE job_id = ?",
                Long.class, jobId);

        Double avgMatchScore = jdbcTemplate.queryForObject(
                "SELECT AVG(ar.match_score) FROM ai_reports ar " +
                "JOIN applications a ON ar.application_id = a.id " +
                "WHERE a.job_id = ?",
                Double.class, jobId);

        Integer topScore = jdbcTemplate.queryForObject(
                "SELECT MAX(ar.match_score) FROM ai_reports ar " +
                "JOIN applications a ON ar.application_id = a.id " +
                "WHERE a.job_id = ?",
                Integer.class, jobId);

        Long pendingReview = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM applications WHERE job_id = ? AND status IN ('PENDING', 'REVIEWED')",
                Long.class, jobId);

        long total = totalApplications != null ? totalApplications : 0L;
        double avg = avgMatchScore != null ? Math.round(avgMatchScore * 10.0) / 10.0 : 0.0;
        int top = topScore != null ? topScore : 0;
        long pending = pendingReview != null ? pendingReview : 0L;

        return new JobStats(total, avg, top, pending);
    }
}
