package com.iarecruiter.gdpr.application.usecase;

import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.gdpr.domain.model.DataRequest;
import com.iarecruiter.gdpr.domain.model.DataRequestStatus;
import com.iarecruiter.gdpr.domain.model.DataRequestType;
import com.iarecruiter.gdpr.domain.port.DataRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitDataRequestUseCase {

    private final DataRequestRepository dataRequestRepository;
    private final ResumeRepository resumeRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Transactional
    public DataRequest execute(UUID candidateId, UUID companyId, DataRequestType requestType) {
        DataRequest request = DataRequest.builder()
                .candidateId(candidateId)
                .companyId(companyId)
                .requestType(requestType)
                .status(DataRequestStatus.PENDING)
                .build();

        DataRequest saved = dataRequestRepository.save(request);

        // Publish event so async processors can handle it
        rabbitTemplate.convertAndSend(exchange, "data.request.new",
                Map.of("dataRequestId", saved.getId().toString(),
                        "candidateId", candidateId.toString(),
                        "companyId", companyId.toString(),
                        "requestType", requestType.name()));

        // For ERASURE: remove resume records immediately and publish deletion event
        if (requestType == DataRequestType.ERASURE) {
            resumeRepository.deleteByCandidate(candidateId);
            rabbitTemplate.convertAndSend(exchange, "data.erasure.resume",
                    Map.of("candidateId", candidateId.toString(),
                            "companyId", companyId.toString()));
        }

        return saved;
    }
}
