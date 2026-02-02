package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_analyses")
public class InterviewAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    // Overall Scores
    private Double overallScore;
    private Double technicalCompetence;
    private Double communicationSkills;
    private Double problemSolvingAbility;
    private Double culturalFitScore;

    // Detailed Analysis
    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(columnDefinition = "TEXT")
    private String hiringRecommendation; // STRONG_HIRE, HIRE, NO_HIRE, BORDERLINE

    // Behavioral Analysis
    private Double confidenceLevel;
    private Double engagementLevel;
    private Double stressTolerance;

    // Technical
    @Column(columnDefinition = "jsonb")
    private String skillAssessments; // {skill: score}

    // Sentiment Analysis
    private Double overallSentiment;
    private String dominantEmotions;

    // Speech Analysis (if audio/video)
    private Double speechClarity;
    private Double speakingRate;
    private Double fillerWordsCount;

    // Metadata
    private LocalDateTime analyzedAt;
    private String analysisModelVersion;
    private Double analysisConfidence;
}
