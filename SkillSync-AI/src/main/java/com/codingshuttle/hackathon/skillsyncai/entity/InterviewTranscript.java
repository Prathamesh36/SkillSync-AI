package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores the full transcript and evaluations for an interview session.
 * Uses JSON columns for flexible message/evaluation storage.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "interview_transcripts")
public class InterviewTranscript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private InterviewSession interviewSession;

    /**
     * JSON array of interview messages.
     * Format: [{"role": "INTERVIEWER|CANDIDATE", "content": "...", "timestamp":
     * "ISO"}]
     */
    @Column(columnDefinition = "TEXT")
    private String messagesJson = "[]";

    /**
     * JSON array of per-question evaluations.
     * Format: [{"questionIndex": 1, "score": 8, "strengths": [...], "weaknesses":
     * [...]}]
     */
    @Column(columnDefinition = "TEXT")
    private String evaluationsJson = "[]";
}
