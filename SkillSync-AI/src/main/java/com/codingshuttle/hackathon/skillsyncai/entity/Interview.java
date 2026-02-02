package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.InterviewMode;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    // Interview Details
    @Enumerated(EnumType.STRING)
    private InterviewType type; // TECHNICAL, BEHAVIORAL, HR, AI_SIMULATION

    @Enumerated(EnumType.STRING)
    private InterviewMode mode; // VIDEO_CALL, PHONE, IN_PERSON, AI_CHAT

    private String title;
    private String description;

    // Scheduling
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String timezone;
    private String meetingLink; // Zoom/Teams link
    private String meetingId;
    private String meetingPasscode;

    // Participants
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id")
    private Recruiter assignedRecruiter;

    @ElementCollection
    @CollectionTable(name = "interview_panelists")
    private List<String> panelistEmails = new ArrayList<>();

    // Status
    @Enumerated(EnumType.STRING)
    private InterviewStatus status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    // AI Interview Fields (for AI-driven interviews)
    private Boolean isAiConducted;
    private String aiInterviewerId;
    private String aiInterviewPrompt;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    @OneToOne(mappedBy = "interview", cascade = CascadeType.ALL)
    private InterviewAnalysis analysis;
}
