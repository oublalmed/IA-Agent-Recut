package com.iarecruiter.candidate.application.usecase;

import com.iarecruiter.candidate.domain.model.Resume;
import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.candidate.domain.port.ResumeStoragePort;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteResumeUseCase {

    private final ResumeRepository resumeRepository;
    private final ResumeStoragePort resumeStoragePort;

    @Transactional
    public void execute(UUID resumeId, UUID companyId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId.toString()));

        if (!resume.getCompanyId().equals(companyId)) {
            throw new BusinessException("Access denied to this resume");
        }

        resumeStoragePort.delete(resume.getMinioObjectKey());
        resumeRepository.deleteById(resumeId);
    }
}
