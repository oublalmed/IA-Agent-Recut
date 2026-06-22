package com.iarecruiter.auth.infrastructure.persistence.jpa;

import com.iarecruiter.auth.infrastructure.persistence.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCompanyRepository extends JpaRepository<CompanyEntity, UUID> {
}
