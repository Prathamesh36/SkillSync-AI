package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String location;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency; // "USD", "INR"

    @Enumerated(EnumType.STRING)
    private JobType jobType; // REMOTE, HYBRID, ONSITE

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType; // FULL_TIME, CONTRACT

    private Integer minExperienceYears;
    private Integer maxExperienceYears;

    @ElementCollection
    private List<String> skillsRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User postedBy;

    private boolean active = true;

    private java.time.LocalDate applicationDeadline;

    @Column(unique = true)
    private String jobReferenceId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
