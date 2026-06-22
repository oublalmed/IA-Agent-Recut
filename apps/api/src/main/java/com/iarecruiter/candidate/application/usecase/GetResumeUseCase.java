package com.iarecruiter.candidate.application.usecase;

import com.iarecruiter.candidate.domain.model.Resume;
import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetResumeUseCase {

    private final ResumeRepository resumeRepository;

    public Resume execute(UUID resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId.toString()));
    }
}
