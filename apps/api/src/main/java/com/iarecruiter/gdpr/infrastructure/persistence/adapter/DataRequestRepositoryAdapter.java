package com.iarecruiter.gdpr.infrastructure.persistence.adapter;

import com.iarecruiter.gdpr.domain.model.DataRequest;
import com.iarecruiter.gdpr.domain.model.DataRequestStatus;
import com.iarecruiter.gdpr.domain.model.DataRequestType;
import com.iarecruiter.gdpr.domain.port.DataRequestRepository;
import com.iarecruiter.gdpr.infrastructure.persistence.entity.DataRequestEntity;
import com.iarecruiter.gdpr.infrastructure.persistence.jpa.JpaDataRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataRequestRepositoryAdapter implements DataRequestRepository {

    private final JpaDataRequestRepository jpa;

    @Override
    public DataRequest save(DataRequest dr) {
        DataRequestEntity entity = DataRequestEntity.builder()
                .id(dr.getId())
                .candidateId(dr.getCandidateId())
                .companyId(dr.getCompanyId())
                .requestType(dr.getRequestType().name())
                .status(dr.getStatus().name())
                .completedAt(dr.getCompletedAt())
                .build();
        DataRequestEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    private DataRequest toDomain(DataRequestEntity e) {
        return DataRequest.builder()
                .id(e.getId())
                .candidateId(e.getCandidateId())
                .companyId(e.getCompanyId())
                .requestType(DataRequestType.valueOf(e.getRequestType()))
                .status(DataRequestStatus.valueOf(e.getStatus()))
                .requestedAt(e.getRequestedAt())
                .completedAt(e.getCompletedAt())
                .build();
    }
}
