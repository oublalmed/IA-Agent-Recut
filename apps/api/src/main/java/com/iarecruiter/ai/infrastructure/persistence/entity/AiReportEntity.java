package com.iarecruiter.ai.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "ai_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiReportEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "application_id", nullable = false, unique = true) private UUID applicationId;
    @Column(name = "match_score", nullable = false) private double matchScore;
    @Column(name = "skill_score") private double skillScore;
    @Column(name = "experience_score") private double experienceScore;
    @Column(name = "education_score") private double educationScore;
    @Column(name = "language_score") private double languageScore;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ai_report_strengths", joinColumns = @JoinColumn(name = "ai_report_id"))
    @Column(name = "strength")
    private List<String> strengths;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ai_report_weaknesses", joinColumns = @JoinColumn(name = "ai_report_id"))
    @Column(name = "weakness")
    private List<String> weaknesses;
    @Column(columnDefinition = "TEXT") private String recommendation;
    @Column(name = "raw_llm_response", columnDefinition = "TEXT") private String rawLlmResponse;
    @Column(name = "model_used") private String modelUsed;
    @Column(name = "tokens_input") private Integer tokensInput;
    @Column(name = "tokens_output") private Integer tokensOutput;
    @CreationTimestamp @Column(name = "generated_at", nullable = false, updatable = false) private Instant generatedAt;
}
