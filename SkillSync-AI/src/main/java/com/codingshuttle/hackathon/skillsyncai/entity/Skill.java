package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.ProficiencyLevel;
import com.codingshuttle.hackathon.skillsyncai.enums.SkillCategory;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Column(nullable = false)
    private String name; // "Java", "Spring Boot", "AWS"

    @Enumerated(EnumType.STRING)
    private SkillCategory category; // TECHNICAL, SOFT, TOOL, LANGUAGE, FRAMEWORK

    @Enumerated(EnumType.STRING)
    private ProficiencyLevel proficiency; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT

    private Integer yearsOfExperience;
    private LocalDateTime lastUsed;
    private boolean isCertified;
    private String certificationName;

    // AI-generated fields
    private Double confidenceScore; // AI's confidence in extraction
    private String sourceContext; // Where in resume it was found
}
