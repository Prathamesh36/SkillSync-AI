package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for job invitation details.
 */
public record InvitationResponseDTO(
        Long invitationId,
        Long jobId,
        String jobTitle,
        String companyName,
        String recruiterName,
        String recruiterMessage,
        JobInvitationStatus status,
        LocalDateTime invitedAt,
        LocalDateTime expiresAt,
        LocalDateTime respondedAt) {
}
