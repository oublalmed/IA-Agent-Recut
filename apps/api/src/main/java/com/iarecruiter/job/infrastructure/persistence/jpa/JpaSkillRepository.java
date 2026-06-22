package com.iarecruiter.job.infrastructure.persistence.jpa;

import com.iarecruiter.job.infrastructure.persistence.entity.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaSkillRepository extends JpaRepository<SkillEntity, UUID> {
    Optional<SkillEntity> findByNameIgnoreCase(String name);
}
