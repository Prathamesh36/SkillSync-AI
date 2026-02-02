package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "screening_results")
public class ScreeningResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    // Overall Scores
    private Double overallScore; // 0-100
    private Double skillMatchScore;
    private Double experienceMatchScore;
    private Double culturalFitScore;

    // AI Feedback
    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    // Requirement Analysis
    @Column(columnDefinition = "jsonb")
    private String requirementAnalysis; // JSON: {requirementId: {met: boolean, score: double}}

    // Flags
    private Boolean passesScreening;
    private Boolean requiresManualReview;
    private String redFlags; // Comma-separated red flags

    // Technical
    private LocalDateTime screenedAt;
    private String screeningModelVersion;
    private Double screeningConfidence;
}
