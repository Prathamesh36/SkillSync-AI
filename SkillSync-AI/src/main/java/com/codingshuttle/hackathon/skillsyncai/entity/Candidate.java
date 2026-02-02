package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
@PrimaryKeyJoinColumn(name = "user_id")
public class Candidate extends User {
    // Additional attributes specific to candidates can be added here

    private String headline; // "Senior Java Developer"
    private String summary;  // Professional summary
    private String currentTitle;
    private String currentCompany;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus; // ACTIVE, LOOKING, NOT_LOOKING

    private BigDecimal expectedSalary;
    private String currency;
    private String location;
    private boolean isRemotePreferred;

    private Integer noticePeriodDays;
    private String portfolioUrl;
    private String githubUrl;
    private String linkedinUrl;

    // AI-generated fields
    @Column(columnDefinition = "TEXT")
    private String aiProfileSummary;

    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL)
    private Resume resume;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Interview> interviews = new ArrayList<>();
}
