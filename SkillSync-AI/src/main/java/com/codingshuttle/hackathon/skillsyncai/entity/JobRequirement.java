package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.ProficiencyLevel;
import com.codingshuttle.hackathon.skillsyncai.enums.RequirementCategory;
import com.codingshuttle.hackathon.skillsyncai.enums.RequirementType;
import jakarta.persistence.*;

@Entity
@Table(name = "job_requirements")
public class JobRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    private RequirementType type; // MUST_HAVE, NICE_TO_HAVE, BONUS

    @Column(nullable = false)
    private String requirement; // "5+ years of Java experience"

    @Enumerated(EnumType.STRING)
    private RequirementCategory category; // SKILL, EXPERIENCE, EDUCATION, CERTIFICATION

    // For skill requirements
    private String skillName;
    private Integer minYears;
    private ProficiencyLevel minProficiency;

    // For education requirements
    private String degree;
    private String fieldOfStudy;
    private String minGpa;

    // Weight for AI matching
    private Double weight; // 0.0 to 1.0
}
