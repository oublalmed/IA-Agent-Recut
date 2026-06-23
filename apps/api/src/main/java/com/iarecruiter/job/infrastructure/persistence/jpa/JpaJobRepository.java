package com.iarecruiter.job.infrastructure.persistence.jpa;

import com.iarecruiter.job.domain.model.JobStatus;
import com.iarecruiter.job.infrastructure.persistence.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaJobRepository extends JpaRepository<JobEntity, UUID> {
    List<JobEntity> findByCompanyId(UUID companyId);
    List<JobEntity> findByCompanyIdAndStatus(UUID companyId, JobStatus status);

    @Query("SELECT j FROM JobEntity j WHERE j.companyId = :companyId " +
           "AND (:status IS NULL OR j.status = :status) " +
           "AND (:search IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<JobEntity> search(@Param("companyId") UUID companyId,
                           @Param("status") JobStatus status,
                           @Param("search") String search);
}
