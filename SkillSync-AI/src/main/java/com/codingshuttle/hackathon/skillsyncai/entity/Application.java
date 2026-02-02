package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationSource;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    // Application Details
    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    private ApplicationSource source; // WEBSITE, LINKEDIN, REFERRAL, AGENCY

    private String referralName;
    private String referralEmail;

    // Status Tracking
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status; // SUBMITTED, SCREENING, SHORTLISTED, REJECTED, HIRED

    private LocalDateTime appliedAt;
    private LocalDateTime statusUpdatedAt;
    private String statusNotes;

    // AI Screening Results
    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL)
    private ScreeningResult screeningResult;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL)
    private MatchingScore matchingScore;

    // Recruiter Actions
    private Boolean isStarred;
    private String recruiterNotes;
    private Integer rating; // 1-5 stars

    // Communication
    private LocalDateTime lastContactedAt;
    private String nextStep;
    private LocalDateTime nextFollowUpDate;

    @OneToMany(mappedBy = "application")
    private List<Interview> interviews = new ArrayList<>();
}
