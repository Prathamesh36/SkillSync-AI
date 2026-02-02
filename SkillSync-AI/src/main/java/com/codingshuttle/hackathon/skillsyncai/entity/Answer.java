package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String audioUrl; // For voice responses
    private String videoUrl; // For video responses

    private Integer timeTakenSeconds;
    private LocalDateTime answeredAt;

    // For AI analysis
    @Column(columnDefinition = "jsonb")
    private String aiAnalysis; // JSON with detailed analysis

    private Double confidenceScore;
    private Double technicalAccuracy;
    private Double communicationClarity;
    private Double problemSolvingScore;

    private String feedback;
    private String suggestedImprovements;

    private LocalDateTime analyzedAt;
    private String analysisModelVersion;
}
