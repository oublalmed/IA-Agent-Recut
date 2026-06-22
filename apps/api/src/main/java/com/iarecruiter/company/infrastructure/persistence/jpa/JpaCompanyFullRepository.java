package com.iarecruiter.company.infrastructure.persistence.jpa;

import com.iarecruiter.auth.infrastructure.persistence.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCompanyFullRepository extends JpaRepository<CompanyEntity, UUID> {
}
