package com.iarecruiter.job.application.usecase;

import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.port.JobRepository;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetJobUseCase {

    private final JobRepository jobRepository;

    public Job execute(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId.toString()));
    }
}
