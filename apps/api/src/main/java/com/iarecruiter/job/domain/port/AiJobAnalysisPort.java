package com.iarecruiter.job.domain.port;

import com.iarecruiter.job.domain.model.JobAnalysis;

public interface AiJobAnalysisPort {
    JobAnalysis analyzeJob(String jobTitle, String jobDescription);
}
