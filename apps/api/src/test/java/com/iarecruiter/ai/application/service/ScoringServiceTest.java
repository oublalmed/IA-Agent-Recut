package com.iarecruiter.ai.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ScoringServiceTest {

    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService(new ObjectMapper());
    }

    private static final String JOB_ANALYSIS_JSON = """
            {
              "requiredSkills": [
                {"name": "Java", "category": "TECHNICAL", "weight": 1.0, "mandatory": true},
                {"name": "Spring Boot", "category": "TECHNICAL", "weight": 0.9, "mandatory": true},
                {"name": "PostgreSQL", "category": "TECHNICAL", "weight": 0.7, "mandatory": false}
              ],
              "niceToHaveSkills": [
                {"name": "Docker", "category": "TOOL", "weight": 0.4, "mandatory": false}
              ],
              "minExperienceYears": 3,
              "educationRequirements": ["computer science", "software engineering"],
              "languages": ["French", "English"]
            }
            """;

    private static final String RESUME_FULL_MATCH = """
            {
              "skills": [
                {"name": "Java", "category": "TECHNICAL"},
                {"name": "Spring Boot", "category": "TECHNICAL"},
                {"name": "PostgreSQL", "category": "TECHNICAL"},
                {"name": "Docker", "category": "TOOL"}
              ],
              "experiences": [
                {"company": "TechCorp", "title": "Dev", "startDate": "2019-01", "endDate": "2023-01", "current": false}
              ],
              "education": [{"institution": "Univ", "degree": "Bachelor", "field": "computer science"}],
              "languages": [{"language": "French", "level": "NATIVE"}, {"language": "English", "level": "FLUENT"}],
              "totalExperienceYears": 4
            }
            """;

    private static final String RESUME_POOR_MATCH = """
            {
              "skills": [{"name": "PHP", "category": "TECHNICAL"}],
              "experiences": [],
              "education": [],
              "languages": [{"language": "Spanish", "level": "NATIVE"}],
              "totalExperienceYears": 0
            }
            """;

    private static final String RESUME_EMPTY = """
            {"skills": [], "experiences": [], "education": [], "languages": []}
            """;

    @Test
    void score_perfectMatch_returnsHighScore() {
        var result = scoringService.score(JOB_ANALYSIS_JSON, RESUME_FULL_MATCH);
        assertThat(result.overall()).isGreaterThan(80.0);
        assertThat(result.skillScore()).isGreaterThan(90.0);
        assertThat(result.experienceScore()).isGreaterThan(70.0);
        assertThat(result.languageScore()).isEqualTo(100.0);
    }

    @Test
    void score_poorMatch_returnsLowScore() {
        var result = scoringService.score(JOB_ANALYSIS_JSON, RESUME_POOR_MATCH);
        assertThat(result.overall()).isLessThan(40.0);
        assertThat(result.skillScore()).isLessThan(20.0);
    }

    @Test
    void score_emptyResume_returnsLowButNonZeroScore() {
        var result = scoringService.score(JOB_ANALYSIS_JSON, RESUME_EMPTY);
        assertThat(result.overall()).isGreaterThanOrEqualTo(0.0);
        assertThat(result.overall()).isLessThan(40.0);
    }

    @Test
    void score_emptyJobAnalysis_returnsNeutralScore() {
        var result = scoringService.score("{}", RESUME_FULL_MATCH);
        assertThat(result.overall()).isGreaterThan(30.0);
    }

    @Test
    void score_invalidJson_returnsZero() {
        var result = scoringService.score("not-json", "also-not-json");
        assertThat(result.overall()).isEqualTo(0.0);
    }

    @Test
    void score_mandatorySkillMissing_appliesPenalty() {
        String resumeWithoutMandatory = """
                {
                  "skills": [{"name": "PostgreSQL"}, {"name": "Docker"}],
                  "experiences": [{"startDate": "2020-01", "endDate": "2023-01", "current": false}],
                  "education": [],
                  "languages": [{"language": "French"}, {"language": "English"}],
                  "totalExperienceYears": 3
                }
                """;
        var result = scoringService.score(JOB_ANALYSIS_JSON, resumeWithoutMandatory);
        assertThat(result.skillScore()).isLessThan(60.0);
    }
}
