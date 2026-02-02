package com.codingshuttle.hackathon.skillsyncai.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recruiters")
@PrimaryKeyJoinColumn(name = "user_id")
public class Recruiter extends User{

    private String employeeId;
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "postedBy")
    private List<Job> postedJobs = new ArrayList<>();

    @OneToMany(mappedBy = "assignedRecruiter")
    private List<Interview> assignedInterviews = new ArrayList<>();

    // Recruiter metrics
    private Integer totalHires;
    private BigDecimal averageTimeToFill;
    private Integer openPositionsCount;

    // Communication preferences
    private boolean emailNotifications;
    private boolean slackNotifications;
    private String slackWebhookUrl;
}
