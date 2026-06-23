package com.iarecruiter.job.domain.port;

import com.iarecruiter.job.domain.model.JobStats;

import java.util.UUID;

public interface JobStatsRepository {
    JobStats getStatsForJob(UUID jobId);
}
