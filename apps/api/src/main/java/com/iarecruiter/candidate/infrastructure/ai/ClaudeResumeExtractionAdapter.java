package com.iarecruiter.candidate.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iarecruiter.candidate.domain.model.ExtractedResumeData;
import com.iarecruiter.candidate.domain.port.AiResumeExtractionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ClaudeResumeExtractionAdapter implements AiResumeExtractionPort {

    private final WebClient anthropicWebClient;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.model-extraction}")
    private String model;

    @Value("${anthropic.max-tokens}")
    private int maxTokens;

    public ClaudeResumeExtractionAdapter(
            @Qualifier("anthropicWebClient") WebClient anthropicWebClient,
            ObjectMapper objectMapper) {
        this.anthropicWebClient = anthropicWebClient;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
            You are an expert HR assistant. Extract structured information from resume text.
            Respond with valid JSON only. Do NOT infer or include any protected characteristics
            (gender, age, ethnicity, religion, disability, marital status, nationality).
            """;

    private static final String USER_PROMPT = """
            Extract the following information from this resume text and return as JSON:
            {
              "firstName": "string or null",
              "lastName": "string or null",
              "email": "string or null",
              "phone": "string or null",
              "location": "string or null",
              "summary": "brief professional summary or null",
              "totalExperienceYears": number or null,
              "skills": [
                {"name": "skill", "category": "TECHNICAL|SOFT|LANGUAGE|TOOL", "yearsExperience": number or null, "proficiencyLevel": "BEGINNER|INTERMEDIATE|ADVANCED|EXPERT or null"}
              ],
              "experiences": [
                {"company": "string", "title": "string", "startDate": "YYYY-MM or null", "endDate": "YYYY-MM or null", "description": "string or null", "current": boolean}
              ],
              "education": [
                {"institution": "string", "degree": "string", "field": "string or null", "graduationYear": number or null}
              ],
              "languages": [
                {"language": "string", "level": "NATIVE|FLUENT|PROFESSIONAL|BASIC"}
              ]
            }

            Resume text:
            %s
            """;

    @Override
    public ExtractedResumeData extractFromText(String resumeText) {
        // Truncate to avoid token limits (approx 6000 chars ≈ 1500 tokens)
        String truncated = resumeText.length() > 6000 ? resumeText.substring(0, 6000) : resumeText;

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", SYSTEM_PROMPT,
                "messages", List.of(Map.of("role", "user", "content", USER_PROMPT.formatted(truncated)))
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
            log.error("Claude resume extraction failed: {}", e.getMessage());
            return ExtractedResumeData.builder()
                    .skills(List.of()).experiences(List.of())
                    .education(List.of()).languages(List.of()).build();
        }
    }

    private ExtractedResumeData parseResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        String content = root.path("content").get(0).path("text").asText().strip();
        if (content.startsWith("```")) {
            content = content.replaceAll("^```(?:json)?\\n?", "").replaceAll("\\n?```$", "").strip();
        }

        JsonNode data = objectMapper.readTree(content);

        List<ExtractedResumeData.SkillEntry> skills = new ArrayList<>();
        data.path("skills").forEach(s -> skills.add(ExtractedResumeData.SkillEntry.builder()
                .name(s.path("name").asText())
                .category(s.path("category").asText("TECHNICAL"))
                .yearsExperience(s.path("yearsExperience").isNull() ? null : s.path("yearsExperience").asDouble())
                .proficiencyLevel(nullIfEmpty(s.path("proficiencyLevel").asText(null)))
                .build()));

        List<ExtractedResumeData.ExperienceEntry> experiences = new ArrayList<>();
        data.path("experiences").forEach(e -> experiences.add(ExtractedResumeData.ExperienceEntry.builder()
                .company(e.path("company").asText())
                .title(e.path("title").asText())
                .startDate(nullIfEmpty(e.path("startDate").asText(null)))
                .endDate(nullIfEmpty(e.path("endDate").asText(null)))
                .description(nullIfEmpty(e.path("description").asText(null)))
                .current(e.path("current").asBoolean(false))
                .build()));

        List<ExtractedResumeData.EducationEntry> education = new ArrayList<>();
        data.path("education").forEach(e -> education.add(ExtractedResumeData.EducationEntry.builder()
                .institution(e.path("institution").asText())
                .degree(e.path("degree").asText())
                .field(nullIfEmpty(e.path("field").asText(null)))
                .graduationYear(e.path("graduationYear").isNull() ? null : e.path("graduationYear").asInt())
                .build()));

        List<ExtractedResumeData.LanguageEntry> languages = new ArrayList<>();
        data.path("languages").forEach(l -> languages.add(ExtractedResumeData.LanguageEntry.builder()
                .language(l.path("language").asText())
                .level(l.path("level").asText("PROFESSIONAL"))
                .build()));

        return ExtractedResumeData.builder()
                .firstName(nullIfEmpty(data.path("firstName").asText(null)))
                .lastName(nullIfEmpty(data.path("lastName").asText(null)))
                .email(nullIfEmpty(data.path("email").asText(null)))
                .phone(nullIfEmpty(data.path("phone").asText(null)))
                .location(nullIfEmpty(data.path("location").asText(null)))
                .summary(nullIfEmpty(data.path("summary").asText(null)))
                .totalExperienceYears(data.path("totalExperienceYears").isNull() ? null : data.path("totalExperienceYears").asInt())
                .skills(skills).experiences(experiences).education(education).languages(languages)
                .build();
    }

    private String nullIfEmpty(String value) {
        return (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) ? null : value;
    }
}
