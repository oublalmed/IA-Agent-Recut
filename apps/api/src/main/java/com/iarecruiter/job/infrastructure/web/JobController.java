package com.iarecruiter.job.infrastructure.web;

import com.iarecruiter.job.application.usecase.*;
import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.model.JobStats;
import com.iarecruiter.job.domain.model.JobStatus;
import com.iarecruiter.job.domain.port.JobStatsRepository;
import com.iarecruiter.job.infrastructure.web.dto.*;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job posting management")
public class JobController {

    private final CreateJobUseCase createJobUseCase;
    private final GetJobUseCase getJobUseCase;
    private final ListJobsUseCase listJobsUseCase;
    private final UpdateJobUseCase updateJobUseCase;
    private final AnalyzeJobUseCase analyzeJobUseCase;
    private final JobStatsRepository jobStatsRepository;

    @PostMapping
    @Operation(summary = "Create a new job posting")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        Job job = createJobUseCase.execute(principal.getCompanyId(), principal.getId(),
                new CreateJobUseCase.CreateJobCommand(
                        request.title(), request.description(), request.location(),
                        request.remotePolicy(), request.contractType(),
                        request.experienceYearsMin(), request.experienceYearsMax(),
                        request.salaryMin(), request.salaryMax(), request.salaryCurrency()
                ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(job));
    }

    @GetMapping
    @Operation(summary = "List all jobs for the current company (with optional status and search filters)")
    public ResponseEntity<List<JobResponse>> listJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        List<Job> jobs = (status != null || (search != null && !search.isBlank()))
                ? listJobsUseCase.execute(principal.getCompanyId(), status, search)
                : listJobsUseCase.execute(principal.getCompanyId());
        return ResponseEntity.ok(jobs.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID")
    public ResponseEntity<JobResponse> getJob(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        Job job = getJobUseCase.execute(id);
        assertSameCompany(job, principal);
        return ResponseEntity.ok(toResponse(job));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a job posting")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        Job updated = updateJobUseCase.execute(id, principal.getCompanyId(),
                new UpdateJobUseCase.UpdateJobCommand(
                        request.title(), request.description(), request.location(),
                        request.remotePolicy(), request.contractType(),
                        request.experienceYearsMin(), request.experienceYearsMax(),
                        request.salaryMin(), request.salaryMax(), request.status()
                ));
        return ResponseEntity.ok(toResponse(updated));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get statistics for a job (applications count, scores, pending review)")
    public ResponseEntity<JobStats> getJobStats(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        Job job = getJobUseCase.execute(id);
        assertSameCompany(job, principal);
        return ResponseEntity.ok(jobStatsRepository.getStatsForJob(id));
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger AI analysis of a job posting")
    public ResponseEntity<JobResponse> analyzeJob(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        Job analyzed = analyzeJobUseCase.execute(id, principal.getCompanyId());
        return ResponseEntity.ok(toResponse(analyzed));
    }

    private void assertSameCompany(Job job, AuthenticatedUser principal) {
        if (!job.getCompanyId().equals(principal.getCompanyId())) {
            throw new BusinessException("Access denied to this job");
        }
    }

    private JobResponse toResponse(Job j) {
        List<JobResponse.RequiredSkillDto> skills = j.getRequiredSkills() == null ? List.of() :
                j.getRequiredSkills().stream()
                        .map(s -> new JobResponse.RequiredSkillDto(s.getSkillId(), s.getSkillName(), s.getWeight(), s.isMandatory()))
                        .toList();
        return new JobResponse(
                j.getId(), j.getCompanyId(), j.getTitle(), j.getDescription(),
                j.getLocation(), j.getRemotePolicy(), j.getContractType(),
                j.getExperienceYearsMin(), j.getExperienceYearsMax(),
                j.getSalaryMin(), j.getSalaryMax(), j.getSalaryCurrency(),
                j.getStatus(), j.getAiAnalyzedAt(), j.getAiAnalysisJson(),
                skills, j.getCreatedAt(), j.getUpdatedAt()
        );
    }
}
