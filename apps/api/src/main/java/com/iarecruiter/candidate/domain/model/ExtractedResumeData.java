package com.iarecruiter.candidate.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter @Builder
public class ExtractedResumeData {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final String location;
    private final String summary;
    private final List<SkillEntry> skills;
    private final List<ExperienceEntry> experiences;
    private final List<EducationEntry> education;
    private final List<LanguageEntry> languages;
    private final Integer totalExperienceYears;

    @Getter @Builder
    public static class SkillEntry {
        private final String name;
        private final String category;
        private final Double yearsExperience;
        private final String proficiencyLevel;
    }

    @Getter @Builder
    public static class ExperienceEntry {
        private final String company;
        private final String title;
        private final String startDate;
        private final String endDate;
        private final String description;
        private final boolean current;
    }

    @Getter @Builder
    public static class EducationEntry {
        private final String institution;
        private final String degree;
        private final String field;
        private final Integer graduationYear;
    }

    @Getter @Builder
    public static class LanguageEntry {
        private final String language;
        private final String level;
    }
}
