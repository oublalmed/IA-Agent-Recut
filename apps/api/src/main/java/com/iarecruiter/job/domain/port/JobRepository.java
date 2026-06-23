package com.iarecruiter.job.domain.port;

import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.model.JobStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository {
    Job save(Job job);
    Optional<Job> findById(UUID id);
    List<Job> findByCompanyId(UUID companyId);
    List<Job> findByCompanyIdAndStatus(UUID companyId, JobStatus status);
    List<Job> search(UUID companyId, JobStatus status, String titleSearch);
}
