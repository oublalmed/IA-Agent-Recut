package com.iarecruiter.gdpr.infrastructure.persistence.jpa;

import com.iarecruiter.gdpr.infrastructure.persistence.entity.DataRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDataRequestRepository extends JpaRepository<DataRequestEntity, UUID> {
}
