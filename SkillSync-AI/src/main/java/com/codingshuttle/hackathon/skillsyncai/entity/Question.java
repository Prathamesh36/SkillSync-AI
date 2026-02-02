package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.QuestionDifficulty;
import com.codingshuttle.hackathon.skillsyncai.enums.QuestionType;
import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id")
    private Interview interview;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    private QuestionType type; // TECHNICAL, BEHAVIORAL, SITUATIONAL, CULTURAL_FIT

    @Enumerated(EnumType.STRING)
    private QuestionDifficulty difficulty; // EASY, MEDIUM, HARD

    private String category; // "Java", "System Design", "Leadership"
    private Integer expectedAnswerTimeSeconds;

    // AI Generation Info
    private Boolean isAiGenerated;
    private String aiModelUsed;
    private String generationPrompt;

    // For adaptive interviews
    private Integer sequenceOrder;
    private String dependsOnQuestionId; // For follow-up questions

    // Candidate's Answer
    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private Answer answer;

    // Ideal Answer (for AI evaluation)
    @Column(columnDefinition = "TEXT")
    private String idealAnswer;

    private String evaluationCriteria;
}
