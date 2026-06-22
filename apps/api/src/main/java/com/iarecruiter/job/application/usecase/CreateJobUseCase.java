package com.iarecruiter.job.application.usecase;

import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.model.JobStatus;
import com.iarecruiter.job.domain.port.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateJobUseCase {

    private final JobRepository jobRepository;

    @Transactional
    public Job execute(UUID companyId, UUID userId, CreateJobCommand command) {
        Job job = Job.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .createdBy(userId)
                .title(command.title())
                .description(command.description())
                .location(command.location())
                .remotePolicy(command.remotePolicy())
                .contractType(command.contractType())
                .experienceYearsMin(command.experienceYearsMin())
                .experienceYearsMax(command.experienceYearsMax())
                .salaryMin(command.salaryMin())
                .salaryMax(command.salaryMax())
                .salaryCurrency(command.salaryCurrency() != null ? command.salaryCurrency() : "EUR")
                .status(JobStatus.DRAFT)
                .requiredSkills(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return jobRepository.save(job);
    }

    public record CreateJobCommand(
            String title,
            String description,
            String location,
            String remotePolicy,
            String contractType,
            Integer experienceYearsMin,
            Integer experienceYearsMax,
            Integer salaryMin,
            Integer salaryMax,
            String salaryCurrency
    ) {}
}
