package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.InterviewSessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a mock interview session for a candidate.
 * Uses UUID for external exposure (safer than auto-increment).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "interview_sessions")
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewSessionStatus status;

    /**
     * Number of questions asked so far in this session.
     * Used to enforce max question limit.
     */
    private int questionCount = 0;

    @CreationTimestamp
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    /**
     * Final aggregated score (0-100) after interview completion.
     */
    private Double finalScore;

    /**
     * AI-generated final feedback summarizing the interview.
     */
    @Column(columnDefinition = "TEXT")
    private String finalFeedback;

    /**
     * Compact resume summary used for AI prompts.
     * Stored to avoid re-computing during session.
     * Only used for RESUME_BASED mode.
     */
    @Column(columnDefinition = "TEXT")
    private String resumeSummary;

    /**
     * Interview mode: RESUME_BASED or TOPIC_BASED.
     * Defaults to RESUME_BASED for backward compatibility.
     * Nullable to support existing records during migration.
     */
    @Enumerated(EnumType.STRING)
    @Column
    private com.codingshuttle.hackathon.skillsyncai.enums.InterviewMode interviewMode = com.codingshuttle.hackathon.skillsyncai.enums.InterviewMode.RESUME_BASED;

    /**
     * JSON array of topics for topic-based interviews.
     * Example: ["Java", "Spring Boot", "SQL"]
     * Only used for TOPIC_BASED mode.
     */
    @Column(columnDefinition = "TEXT")
    private String topicsJson;

    /**
     * Difficulty level for topic-based interviews.
     * Only used for TOPIC_BASED mode.
     */
    @Enumerated(EnumType.STRING)
    private com.codingshuttle.hackathon.skillsyncai.enums.DifficultyLevel difficultyLevel;
}
