package com.iarecruiter.ai.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoringService {

    private final ObjectMapper objectMapper;

    // Weights must sum to 1.0
    private static final double SKILL_WEIGHT       = 0.50;
    private static final double EXPERIENCE_WEIGHT  = 0.30;
    private static final double EDUCATION_WEIGHT   = 0.10;
    private static final double LANGUAGE_WEIGHT    = 0.10;

    public ScoreResult score(String jobAnalysisJson, String resumeExtractedJson) {
        try {
            JsonNode jobAnalysis = objectMapper.readTree(jobAnalysisJson);
            JsonNode resumeData  = objectMapper.readTree(resumeExtractedJson);

            double skillScore      = scoreSkills(jobAnalysis, resumeData);
            double experienceScore = scoreExperience(jobAnalysis, resumeData);
            double educationScore  = scoreEducation(jobAnalysis, resumeData);
            double languageScore   = scoreLanguages(jobAnalysis, resumeData);

            double overall = (skillScore      * SKILL_WEIGHT)
                           + (experienceScore * EXPERIENCE_WEIGHT)
                           + (educationScore  * EDUCATION_WEIGHT)
                           + (languageScore   * LANGUAGE_WEIGHT);

            // Cap at 100
            overall = Math.min(100.0, Math.round(overall * 100.0) / 100.0);

            return new ScoreResult(overall, skillScore, experienceScore, educationScore, languageScore);
        } catch (Exception e) {
            log.warn("Scoring failed, returning zero scores: {}", e.getMessage());
            return new ScoreResult(0, 0, 0, 0, 0);
        }
    }

    private double scoreSkills(JsonNode jobAnalysis, JsonNode resumeData) {
        List<String> requiredSkills = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        boolean hasMandatory = false;
        double mandatoryPenalty = 1.0;

        for (JsonNode skill : jobAnalysis.path("requiredSkills")) {
            requiredSkills.add(skill.path("name").asText("").toLowerCase());
            weights.add(skill.path("weight").asDouble(1.0));
            if (skill.path("mandatory").asBoolean(false)) hasMandatory = true;
        }
        for (JsonNode skill : jobAnalysis.path("niceToHaveSkills")) {
            requiredSkills.add(skill.path("name").asText("").toLowerCase());
            weights.add(skill.path("weight").asDouble(0.5));
        }

        if (requiredSkills.isEmpty()) return 50.0;

        Set<String> candidateSkills = new HashSet<>();
        for (JsonNode skill : resumeData.path("skills")) {
            candidateSkills.add(skill.path("name").asText("").toLowerCase());
        }

        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        double matchedWeight = 0.0;

        for (int i = 0; i < requiredSkills.size(); i++) {
            String reqSkill = requiredSkills.get(i);
            boolean matched = candidateSkills.stream().anyMatch(cs ->
                    cs.contains(reqSkill) || reqSkill.contains(cs));
            if (matched) {
                matchedWeight += weights.get(i);
            }
        }

        if (hasMandatory) {
            for (JsonNode skill : jobAnalysis.path("requiredSkills")) {
                if (skill.path("mandatory").asBoolean(false)) {
                    String name = skill.path("name").asText("").toLowerCase();
                    boolean found = candidateSkills.stream().anyMatch(cs -> cs.contains(name) || name.contains(cs));
                    if (!found) { mandatoryPenalty = 0.6; break; }
                }
            }
        }

        return Math.min(100.0, (matchedWeight / totalWeight) * 100.0 * mandatoryPenalty);
    }

    private double scoreExperience(JsonNode jobAnalysis, JsonNode resumeData) {
        int required = jobAnalysis.path("minExperienceYears").asInt(0);
        if (required == 0) return 80.0;

        int candidateYears = resumeData.path("totalExperienceYears").asInt(-1);
        if (candidateYears < 0) {
            candidateYears = estimateExperienceYears(resumeData);
        }

        if (candidateYears >= required) {
            double extra = Math.min(1.0, (double)(candidateYears - required) / required);
            return Math.min(100.0, 80.0 + extra * 20.0);
        } else {
            double ratio = (double) candidateYears / required;
            return Math.max(0.0, ratio * 80.0);
        }
    }

    private int estimateExperienceYears(JsonNode resumeData) {
        int total = 0;
        for (JsonNode exp : resumeData.path("experiences")) {
            String start = exp.path("startDate").asText(null);
            String end = exp.path("endDate").asText(null);
            boolean current = exp.path("current").asBoolean(false);
            if (start != null && start.length() >= 4) {
                try {
                    int startYear = Integer.parseInt(start.substring(0, 4));
                    int endYear = current ? java.time.Year.now().getValue()
                            : (end != null && end.length() >= 4 ? Integer.parseInt(end.substring(0, 4)) : startYear);
                    total += Math.max(0, endYear - startYear);
                } catch (NumberFormatException ignored) {}
            }
        }
        return total;
    }

    private double scoreEducation(JsonNode jobAnalysis, JsonNode resumeData) {
        if (!resumeData.path("education").isArray() || resumeData.path("education").isEmpty()) return 40.0;

        List<String> requirements = new ArrayList<>();
        for (JsonNode req : jobAnalysis.path("educationRequirements")) {
            requirements.add(req.asText("").toLowerCase());
        }
        if (requirements.isEmpty()) return 70.0;

        Set<String> candidateFields = new HashSet<>();
        for (JsonNode edu : resumeData.path("education")) {
            candidateFields.add(edu.path("field").asText("").toLowerCase());
            candidateFields.add(edu.path("degree").asText("").toLowerCase());
        }

        for (String req : requirements) {
            boolean matched = candidateFields.stream().anyMatch(f -> f.contains(req) || req.contains(f));
            if (matched) return 90.0;
        }
        return 60.0;
    }

    private double scoreLanguages(JsonNode jobAnalysis, JsonNode resumeData) {
        List<String> requiredLangs = new ArrayList<>();
        for (JsonNode lang : jobAnalysis.path("languages")) {
            requiredLangs.add(lang.asText("").toLowerCase());
        }
        if (requiredLangs.isEmpty()) return 80.0;

        Set<String> candidateLangs = new HashSet<>();
        for (JsonNode lang : resumeData.path("languages")) {
            candidateLangs.add(lang.path("language").asText("").toLowerCase());
        }

        long matched = requiredLangs.stream()
                .filter(r -> candidateLangs.stream().anyMatch(c -> c.contains(r) || r.contains(c)))
                .count();

        return Math.min(100.0, ((double) matched / requiredLangs.size()) * 100.0);
    }

    public record ScoreResult(
            double overall,
            double skillScore,
            double experienceScore,
            double educationScore,
            double languageScore
    ) {}
}
