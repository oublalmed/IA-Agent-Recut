package com.iarecruiter.job.application.usecase;

import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.model.JobStatus;
import com.iarecruiter.job.domain.port.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListJobsUseCase {

    private final JobRepository jobRepository;

    public List<Job> execute(UUID companyId) {
        return jobRepository.findByCompanyId(companyId);
    }

    public List<Job> execute(UUID companyId, JobStatus status, String search) {
        return jobRepository.search(companyId, status, search);
    }
}
