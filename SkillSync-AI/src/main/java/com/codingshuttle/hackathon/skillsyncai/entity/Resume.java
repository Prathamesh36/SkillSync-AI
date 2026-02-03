package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "resumes")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String fileName;
    private String fileType;
    private String s3Url; // Or local path

    @Column(columnDefinition = "TEXT")
    private String parsedContent;

    @ElementCollection
    private List<String> extractedSkills;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
