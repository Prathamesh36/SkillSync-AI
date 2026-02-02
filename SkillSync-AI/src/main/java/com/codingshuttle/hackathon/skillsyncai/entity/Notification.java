package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.NotificationCategory;
import com.codingshuttle.hackathon.skillsyncai.enums.NotificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // EMAIL, SMS, PUSH, IN_APP

    @Enumerated(EnumType.STRING)
    private NotificationCategory category; // APPLICATION, INTERVIEW, JOB_ALERT, SYSTEM

    private String actionUrl;
    private String referenceId; // ID of related entity (job, application, etc.)

    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private boolean isRead;

    private String sender; // "system", "recruiter@company.com"
    private String metadata; // JSON with additional data
}
