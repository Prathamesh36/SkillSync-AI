package com.codingshuttle.hackathon.skillsyncai.entity;


import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.WorkLocationType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_id")
    private Recruiter postedBy;

    // Job Details
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP

    @Enumerated(EnumType.STRING)
    private WorkLocationType workLocationType; // ONSITE, REMOTE, HYBRID

    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String salaryPeriod; // YEARLY, MONTHLY, HOURLY

    // Status & Dates
    @Enumerated(EnumType.STRING)
    private JobStatus status; // DRAFT, PUBLISHED, CLOSED, FILLED

    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;
    private LocalDateTime expiresAt;

    // AI Fields
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding; // Job description embedding

    @Column(columnDefinition = "jsonb")
    private String aiExtractedRequirements; // Structured requirements from AI

    // Relationships
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobRequirement> requirementsList = new ArrayList<>();

    @OneToMany(mappedBy = "job")
    private List<Application> applications = new ArrayList<>();

    // Analytics
    private Integer viewCount;
    private Integer applicationCount;
    private Integer shortlistedCount;
}
