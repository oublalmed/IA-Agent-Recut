package com.iarecruiter.job.infrastructure.persistence.jpa;

import com.iarecruiter.job.domain.model.JobStatus;
import com.iarecruiter.job.infrastructure.persistence.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaJobRepository extends JpaRepository<JobEntity, UUID> {
    List<JobEntity> findByCompanyId(UUID companyId);
    List<JobEntity> findByCompanyIdAndStatus(UUID companyId, JobStatus status);
}
