package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a candidate's application for a specific job.
 * Enforces one application per candidate per job via unique constraint.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "job_applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "job_id", "candidate_id" }, name = "uk_job_applications_job_candidate")
})
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    /**
     * Snapshot of match score at the time of application (if available).
     * This is NOT re-calculated; it's fetched from existing MatchResult.
     */
    private Double matchScoreSnapshot;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    @CreationTimestamp
    private LocalDateTime appliedAt;
}
