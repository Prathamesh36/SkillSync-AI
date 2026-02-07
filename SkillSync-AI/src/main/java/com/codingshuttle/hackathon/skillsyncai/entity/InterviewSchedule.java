package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.RealInterviewMode;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewScheduleStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.LastUpdatedBy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for scheduled real 1-on-1 interviews (NOT AI mock interviews).
 * Links an application to a scheduled interview with a recruiter.
 * 
 * Audit fields track reschedule/cancel history without deleting records.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "interview_schedules")
public class InterviewSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The job application this interview is for.
     * One application can have one active interview.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application jobApplication;

    /**
     * The recruiter scheduling the interview.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private Recruiter recruiter;

    /**
     * The candidate being interviewed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /**
     * Scheduled date and time for the interview.
     */
    @Column(nullable = false)
    private LocalDateTime interviewDateTime;

    /**
     * Duration in minutes.
     */
    @Column(nullable = false)
    private int durationMinutes;

    /**
     * Interview mode: ONLINE or OFFLINE.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RealInterviewMode mode;

    /**
     * Meeting link for online interviews.
     * Nullable for offline interviews.
     */
    @Column
    private String meetingLink;

    /**
     * Status of the scheduled interview.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewScheduleStatus status = InterviewScheduleStatus.SCHEDULED;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // ===================== AUDIT FIELDS =====================

    /**
     * Previous interview date/time before reschedule.
     * Populated when interview is rescheduled.
     */
    @Column
    private LocalDateTime previousInterviewDateTime;

    /**
     * Timestamp when interview was rescheduled.
     */
    @Column
    private LocalDateTime rescheduledAt;

    /**
     * Timestamp when interview was cancelled.
     */
    @Column
    private LocalDateTime cancelledAt;

    /**
     * Reason for cancellation.
     */
    @Column
    private String cancellationReason;

    /**
     * Who last updated this interview (RECRUITER or CANDIDATE).
     */
    @Enumerated(EnumType.STRING)
    @Column
    private LastUpdatedBy lastUpdatedBy;
}
