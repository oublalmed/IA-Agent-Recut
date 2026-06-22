package com.iarecruiter.job.application.usecase;

import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.model.JobStatus;
import com.iarecruiter.job.domain.port.JobRepository;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateJobUseCase {

    private final JobRepository jobRepository;

    @Transactional
    public Job execute(UUID jobId, UUID companyId, UpdateJobCommand command) {
        Job existing = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId.toString()));

        if (!existing.getCompanyId().equals(companyId)) {
            throw new BusinessException("Access denied to this job");
        }
        if (existing.getStatus() == JobStatus.CLOSED) {
            throw new BusinessException("Cannot update a closed job");
        }

        Job updated = existing
                .withTitle(command.title() != null ? command.title() : existing.getTitle())
                .withDescription(command.description() != null ? command.description() : existing.getDescription())
                .withLocation(command.location() != null ? command.location() : existing.getLocation())
                .withRemotePolicy(command.remotePolicy() != null ? command.remotePolicy() : existing.getRemotePolicy())
                .withContractType(command.contractType() != null ? command.contractType() : existing.getContractType())
                .withExperienceYearsMin(command.experienceYearsMin() != null ? command.experienceYearsMin() : existing.getExperienceYearsMin())
                .withExperienceYearsMax(command.experienceYearsMax() != null ? command.experienceYearsMax() : existing.getExperienceYearsMax())
                .withSalaryMin(command.salaryMin() != null ? command.salaryMin() : existing.getSalaryMin())
                .withSalaryMax(command.salaryMax() != null ? command.salaryMax() : existing.getSalaryMax())
                .withStatus(command.status() != null ? command.status() : existing.getStatus())
                .withUpdatedAt(Instant.now());

        return jobRepository.save(updated);
    }

    public record UpdateJobCommand(
            String title,
            String description,
            String location,
            String remotePolicy,
            String contractType,
            Integer experienceYearsMin,
            Integer experienceYearsMax,
            Integer salaryMin,
            Integer salaryMax,
            JobStatus status
    ) {}
}
