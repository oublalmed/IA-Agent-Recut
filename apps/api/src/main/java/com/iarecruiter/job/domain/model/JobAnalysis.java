package com.iarecruiter.job.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JobAnalysis {
    private final String jobTitle;
    private final String summary;
    private final List<SkillRequirement> requiredSkills;
    private final List<SkillRequirement> niceToHaveSkills;
    private final int minExperienceYears;
    private final List<String> educationRequirements;
    private final List<String> languages;
    private final String remotePolicy;
    private final String contractType;
    private final String seniorityLevel;

    @Getter
    @Builder
    public static class SkillRequirement {
        private final String name;
        private final String category;
        private final double weight;
        private final boolean mandatory;
    }
}
