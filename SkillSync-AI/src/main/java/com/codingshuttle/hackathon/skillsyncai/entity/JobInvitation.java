package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents an invitation sent by a recruiter to a candidate to apply for a
 * job.
 * 
 * Key invariants:
 * - invitationToken is unique and used for secure accept/decline links
 * - Invitation expires after expiresAt
 * - Status transitions: SENT → ACCEPTED/DECLINED/EXPIRED
 */
@Entity
@Table(name = "job_invitations", indexes = {
        @Index(name = "idx_invitation_token", columnList = "invitationToken", unique = true),
        @Index(name = "idx_invitation_candidate", columnList = "candidate_id"),
        @Index(name = "idx_invitation_job_candidate", columnList = "job_id, candidate_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private Recruiter invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobInvitationStatus status;

    @Column(nullable = false)
    private LocalDateTime invitedAt;

    @Column
    private LocalDateTime respondedAt;


    @Column(length = 1000)
    private String message;


    @Column(nullable = false, unique = true, length = 64)
    private String invitationToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canRespond() {
        return status == JobInvitationStatus.SENT && !isExpired();
    }
}
