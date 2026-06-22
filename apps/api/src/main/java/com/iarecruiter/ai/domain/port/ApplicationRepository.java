package com.iarecruiter.ai.domain.port;

import com.iarecruiter.ai.domain.model.Application;
import com.iarecruiter.ai.domain.model.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository {
    Application save(Application application);
    Optional<Application> findById(UUID id);
    List<Application> findByJobId(UUID jobId);
    Optional<Application> findByJobIdAndCandidateId(UUID jobId, UUID candidateId);
}
