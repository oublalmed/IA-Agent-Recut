package com.iarecruiter.job.infrastructure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iarecruiter.job.domain.model.JobAnalysis;
import com.iarecruiter.job.domain.port.AiJobAnalysisPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaudeJobAnalysisAdapter implements AiJobAnalysisPort {

    private final WebClient anthropicWebClient;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.model-extraction}")
    private String model;

    @Value("${anthropic.max-tokens}")
    private int maxTokens;

    private static final String SYSTEM_PROMPT = """
            You are an expert HR analyst. Your task is to analyze job descriptions and extract structured information.
            Always respond with valid JSON only, no additional text.
            IMPORTANT: Never include or infer protected characteristics (gender, age, ethnicity, religion, disability) in your analysis.
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Analyze the following job posting and return a JSON object with this exact structure:
            {
              "jobTitle": "normalized job title",
              "summary": "2-3 sentence summary of the role",
              "requiredSkills": [
                {"name": "skill name", "category": "TECHNICAL|SOFT|LANGUAGE|TOOL", "weight": 0.0-1.0, "mandatory": true|false}
              ],
              "niceToHaveSkills": [
                {"name": "skill name", "category": "TECHNICAL|SOFT|LANGUAGE|TOOL", "weight": 0.0-1.0, "mandatory": false}
              ],
              "minExperienceYears": 0,
              "educationRequirements": ["requirement1"],
              "languages": ["French", "English"],
              "remotePolicy": "ONSITE|HYBRID|REMOTE|null",
              "contractType": "CDI|CDD|FREELANCE|INTERNSHIP|null",
              "seniorityLevel": "JUNIOR|MID|SENIOR|LEAD|null"
            }
            
            Job Title: %s
            
            Job Description:
            %s
            """;

    @Override
    public JobAnalysis analyzeJob(String jobTitle, String jobDescription) {
        String prompt = USER_PROMPT_TEMPLATE.formatted(jobTitle, jobDescription);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", SYSTEM_PROMPT,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            String response = anthropicWebClient.post()
                    .uri("/v1/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseResponse(response);
        } catch (Exception e) {
            log.error("Failed to analyze job with Claude: {}", e.getMessage());
            return fallbackAnalysis(jobTitle);
        }
    }

    private JobAnalysis parseResponse(String response) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(response);
        String content = root.path("content").get(0).path("text").asText();

        // Strip markdown code fences if present
        content = content.strip();
        if (content.startsWith("```")) {
            content = content.replaceAll("^```(?:json)?\\n?", "").replaceAll("\\n?```$", "").strip();
        }

        JsonNode analysis = objectMapper.readTree(content);

        List<JobAnalysis.SkillRequirement> required = new ArrayList<>();
        analysis.path("requiredSkills").forEach(s -> required.add(JobAnalysis.SkillRequirement.builder()
                .name(s.path("name").asText())
                .category(s.path("category").asText("TECHNICAL"))
                .weight(s.path("weight").asDouble(1.0))
                .mandatory(s.path("mandatory").asBoolean(true))
                .build()));

        List<JobAnalysis.SkillRequirement> niceToHave = new ArrayList<>();
        analysis.path("niceToHaveSkills").forEach(s -> niceToHave.add(JobAnalysis.SkillRequirement.builder()
                .name(s.path("name").asText())
                .category(s.path("category").asText("TECHNICAL"))
                .weight(s.path("weight").asDouble(0.5))
                .mandatory(false)
                .build()));

        List<String> education = new ArrayList<>();
        analysis.path("educationRequirements").forEach(e -> education.add(e.asText()));

        List<String> languages = new ArrayList<>();
        analysis.path("languages").forEach(l -> languages.add(l.asText()));

        return JobAnalysis.builder()
                .jobTitle(analysis.path("jobTitle").asText(null))
                .summary(analysis.path("summary").asText(null))
                .requiredSkills(required)
                .niceToHaveSkills(niceToHave)
                .minExperienceYears(analysis.path("minExperienceYears").asInt(0))
                .educationRequirements(education)
                .languages(languages)
                .remotePolicy(nullIfEmpty(analysis.path("remotePolicy").asText(null)))
                .contractType(nullIfEmpty(analysis.path("contractType").asText(null)))
                .seniorityLevel(nullIfEmpty(analysis.path("seniorityLevel").asText(null)))
                .build();
    }

    private JobAnalysis fallbackAnalysis(String jobTitle) {
        return JobAnalysis.builder()
                .jobTitle(jobTitle)
                .summary("Analysis unavailable")
                .requiredSkills(List.of())
                .niceToHaveSkills(List.of())
                .minExperienceYears(0)
                .educationRequirements(List.of())
                .languages(List.of())
                .build();
    }

    private String nullIfEmpty(String value) {
        return (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) ? null : value;
    }
}
