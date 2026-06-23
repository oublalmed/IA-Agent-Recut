package com.iarecruiter.job.infrastructure.persistence.adapter;

import com.iarecruiter.job.domain.model.Job;
import com.iarecruiter.job.domain.model.JobStatus;
import com.iarecruiter.job.domain.model.RequiredSkill;
import com.iarecruiter.job.domain.port.JobRepository;
import com.iarecruiter.job.infrastructure.persistence.entity.JobEntity;
import com.iarecruiter.job.infrastructure.persistence.entity.JobRequiredSkillEntity;
import com.iarecruiter.job.infrastructure.persistence.jpa.JpaJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JobRepositoryAdapter implements JobRepository {

    private final JpaJobRepository jpa;

    @Override
    public Job save(Job job) {
        JobEntity entity = toEntity(job);
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Job> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Job> findByCompanyId(UUID companyId) {
        return jpa.findByCompanyId(companyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Job> findByCompanyIdAndStatus(UUID companyId, JobStatus status) {
        return jpa.findByCompanyIdAndStatus(companyId, status).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Job> search(UUID companyId, JobStatus status, String titleSearch) {
        return jpa.search(companyId, status, titleSearch).stream().map(this::toDomain).toList();
    }

    private Job toDomain(JobEntity e) {
        List<RequiredSkill> skills = e.getRequiredSkills().stream()
                .map(s -> RequiredSkill.builder()
                        .id(s.getId())
                        .skillId(s.getSkillId())
                        .skillName(s.getSkillName())
                        .weight(s.getWeight())
                        .mandatory(s.isMandatory())
                        .build())
                .toList();
        return Job.builder()
                .id(e.getId())
                .companyId(e.getCompanyId())
                .createdBy(e.getCreatedBy())
                .title(e.getTitle())
                .description(e.getDescription())
                .location(e.getLocation())
                .remotePolicy(e.getRemotePolicy())
                .contractType(e.getContractType())
                .experienceYearsMin(e.getExperienceYearsMin())
                .experienceYearsMax(e.getExperienceYearsMax())
                .salaryMin(e.getSalaryMin())
                .salaryMax(e.getSalaryMax())
                .salaryCurrency(e.getSalaryCurrency())
                .status(e.getStatus())
                .aiAnalyzedAt(e.getAiAnalyzedAt())
                .aiAnalysisJson(e.getAiAnalysisJson())
                .requiredSkills(skills)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private JobEntity toEntity(Job j) {
        JobEntity entity = JobEntity.builder()
                .id(j.getId())
                .companyId(j.getCompanyId())
                .createdBy(j.getCreatedBy())
                .title(j.getTitle())
                .description(j.getDescription())
                .location(j.getLocation())
                .remotePolicy(j.getRemotePolicy())
                .contractType(j.getContractType())
                .experienceYearsMin(j.getExperienceYearsMin())
                .experienceYearsMax(j.getExperienceYearsMax())
                .salaryMin(j.getSalaryMin())
                .salaryMax(j.getSalaryMax())
                .salaryCurrency(j.getSalaryCurrency())
                .status(j.getStatus())
                .aiAnalyzedAt(j.getAiAnalyzedAt())
                .aiAnalysisJson(j.getAiAnalysisJson())
                .build();

        if (j.getRequiredSkills() != null) {
            j.getRequiredSkills().forEach(skill -> {
                JobRequiredSkillEntity skillEntity = JobRequiredSkillEntity.builder()
                        .id(skill.getId())
                        .job(entity)
                        .skillId(skill.getSkillId())
                        .skillName(skill.getSkillName())
                        .weight(skill.getWeight())
                        .mandatory(skill.isMandatory())
                        .build();
                entity.getRequiredSkills().add(skillEntity);
            });
        }
        return entity;
    }
}
