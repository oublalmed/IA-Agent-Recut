package com.iarecruiter.candidate.domain.port;

import com.iarecruiter.candidate.domain.model.ExtractedResumeData;

public interface AiResumeExtractionPort {
    ExtractedResumeData extractFromText(String resumeText);
}
