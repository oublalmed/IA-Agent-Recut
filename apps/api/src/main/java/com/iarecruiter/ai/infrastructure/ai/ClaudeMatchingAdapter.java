package com.iarecruiter.ai.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iarecruiter.ai.domain.model.AiReport;
import com.iarecruiter.ai.domain.port.AiMatchingPort;
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
public class ClaudeMatchingAdapter implements AiMatchingPort {

    private final WebClient anthropicWebClient;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.model-matching}")
    private String model;

    @Value("${anthropic.max-tokens}")
    private int maxTokens;

    public ClaudeMatchingAdapter(
            @Qualifier("anthropicWebClient") WebClient anthropicWebClient,
            ObjectMapper objectMapper) {
        this.anthropicWebClient = anthropicWebClient;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT =
            "You are an expert HR analyst. Analyze the match between a job posting and a candidate's resume. " +
            "Be objective and concise. Never mention or infer protected characteristics (age, gender, ethnicity, religion, disability). " +
            "Respond with valid JSON only.";

    @Override
    public AiReport generateAnalysis(MatchingContext ctx) {
        String prompt = String.format(
            "Based on the pre-computed scores below, provide a brief analysis of this candidate-job match.\n\n" +
            "Job: %s\nOverall match score: %.1f/100\nSkill score: %.1f/100\n" +
            "Experience score: %.1f/100\nEducation score: %.1f/100\nLanguage score: %.1f/100\n\n" +
            "Job requirements (JSON): %s\nCandidate profile (JSON): %s\n\n" +
            "Return JSON with:\n{\n  \"strengths\": [\"strength 1\", \"strength 2\", \"strength 3\"],\n" +
            "  \"weaknesses\": [\"weakness 1\", \"weakness 2\"],\n" +
            "  \"recommendation\": \"2-3 sentence recommendation for the recruiter\"\n}",
            ctx.jobTitle(), ctx.overallScore(),
            ctx.skillScore(), ctx.experienceScore(),
            ctx.educationScore(), ctx.languageScore(),
            truncate(ctx.jobAnalysisJson(), 1500),
            truncate(ctx.resumeExtractedJson(), 2000)
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", Math.min(maxTokens, 1024),
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

            return parseResponse(response, ctx.overallScore());
        } catch (Exception e) {
            log.error("Claude matching analysis failed: {}", e.getMessage());
            return AiReport.builder()
                    .matchScore(ctx.overallScore())
                    .strengths(List.of())
                    .weaknesses(List.of())
                    .recommendation("Automated analysis unavailable. Please review manually.")
                    .modelUsed(model)
                    .generatedAt(java.time.Instant.now())
                    .build();
        }
    }

    private AiReport parseResponse(String response, double overallScore) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        String content = root.path("content").get(0).path("text").asText().strip();
        if (content.startsWith("```")) {
            content = content.replaceAll("^```(?:json)?\\n?", "").replaceAll("\\n?```$", "").strip();
        }

        JsonNode data = objectMapper.readTree(content);
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        data.path("strengths").forEach(s -> strengths.add(s.asText()));
        data.path("weaknesses").forEach(w -> weaknesses.add(w.asText()));

        int inputTokens = root.path("usage").path("input_tokens").asInt(0);
        int outputTokens = root.path("usage").path("output_tokens").asInt(0);

        return AiReport.builder()
                .matchScore(overallScore)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .recommendation(data.path("recommendation").asText(null))
                .rawLlmResponse(response)
                .modelUsed(root.path("model").asText(model))
                .tokensInput(inputTokens)
                .tokensOutput(outputTokens)
                .generatedAt(java.time.Instant.now())
                .build();
    }

    private String truncate(String s, int maxChars) {
        if (s == null) return "{}";
        return s.length() > maxChars ? s.substring(0, maxChars) + "..." : s;
    }
}
