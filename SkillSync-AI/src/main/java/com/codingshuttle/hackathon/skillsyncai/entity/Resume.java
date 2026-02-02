package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    // Original file info
    private String originalFileName;
    private String fileType; // PDF, DOC, DOCX
    private Long fileSize;
    private String storagePath;
    private LocalDateTime uploadedAt;

    // Raw and parsed content
    @Column(columnDefinition = "TEXT")
    private String rawText; // Extracted text from file

    @Column(columnDefinition = "jsonb")
    private String parsedJson; // AI-parsed structured JSON

    // AI Embeddings for vector search
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding; // For semantic search

    private LocalDateTime lastParsedAt;
    private String parsingVersion; // AI model version used

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<Education> educations = new ArrayList<>();

    // AI Analysis fields
    private Double experienceScore;
    private Double skillRelevanceScore;
    private String aiFeedback;

    @OneToMany(mappedBy = "resume")
    private List<Application> applications = new ArrayList<>();
}
