package com.iarecruiter.ai.infrastructure.persistence.adapter;

import com.iarecruiter.ai.domain.model.Application;
import com.iarecruiter.ai.domain.port.ApplicationRepository;
import com.iarecruiter.ai.infrastructure.persistence.entity.ApplicationEntity;
import com.iarecruiter.ai.infrastructure.persistence.jpa.JpaApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ApplicationRepositoryAdapter implements ApplicationRepository {
    private final JpaApplicationRepository jpa;

    @Override public Application save(Application a) { return toDomain(jpa.save(toEntity(a))); }
    @Override public Optional<Application> findById(UUID id) { return jpa.findById(id).map(this::toDomain); }
    @Override public List<Application> findByJobId(UUID jobId) { return jpa.findByJobId(jobId).stream().map(this::toDomain).toList(); }
    @Override public Optional<Application> findByJobIdAndCandidateId(UUID j, UUID c) { return jpa.findByJobIdAndCandidateId(j, c).map(this::toDomain); }

    private Application toDomain(ApplicationEntity e) {
        return Application.builder().id(e.getId()).companyId(e.getCompanyId()).jobId(e.getJobId())
                .candidateId(e.getCandidateId()).resumeId(e.getResumeId()).status(e.getStatus())
                .appliedAt(e.getAppliedAt()).updatedAt(e.getUpdatedAt()).build();
    }
    private ApplicationEntity toEntity(Application a) {
        return ApplicationEntity.builder().id(a.getId()).companyId(a.getCompanyId()).jobId(a.getJobId())
                .candidateId(a.getCandidateId()).resumeId(a.getResumeId()).status(a.getStatus()).build();
    }
}
