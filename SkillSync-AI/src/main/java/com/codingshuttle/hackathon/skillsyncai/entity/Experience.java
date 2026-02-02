package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "experiences")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    private String company;
    private String title;
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;

    @ElementCollection
    @CollectionTable(name = "experience_achievements")
    private List<String> achievements = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "experience_skills",
            joinColumns = @JoinColumn(name = "experience_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skillsUsed = new ArrayList<>();

    // AI Analysis
    private String industry;
    private String companySize;
    private boolean isVerified; // If verified through LinkedIn/background check
    private Double relevanceScore;
}
