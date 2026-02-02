package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching_scores")
public class MatchingScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    // Vector Similarity Scores
    private Double semanticSimilarity; // Cosine similarity of embeddings
    private Double keywordMatchScore;
    private Double experienceLevelMatch;

    // Domain-specific scores
    private Double industryRelevance;
    private Double companyCultureFit;
    private Double locationCompatibility;
    private Double salaryExpectationMatch;

    // Composite Scores
    private Double totalScore;
    private Integer rankAmongCandidates;
    private Double percentile;

    // AI Explanations
    @Column(columnDefinition = "TEXT")
    private String matchExplanation;

    private String topMatchingSkills;
    private String missingRequirements;

    // Metadata
    private LocalDateTime calculatedAt;
    private String matchingAlgorithmVersion;
}
