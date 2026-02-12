package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Job invitation details")
public record InvitationResponseDTO(
        @Schema(description = "Invitation ID") Long id,
        @Schema(description = "Job ID") Long jobId,
        @Schema(description = "Job title") String jobTitle,
        @Schema(description = "Company name") String companyName,
        @Schema(description = "Recruiter name") String recruiterName,
        @Schema(description = "Recruiter email") String recruiterEmail,
        @Schema(description = "Personalized invitation message") String message,
        @Schema(description = "Current invitation status") JobInvitationStatus status,
        @Schema(description = "Secure token for accept/decline") String token,
        @Schema(description = "Invitation sent timestamp") LocalDateTime sentAt,
        @Schema(description = "Invitation expiration timestamp") LocalDateTime expiresAt,
        @Schema(description = "Candidate user ID") Long candidateId,
        @Schema(description = "Candidate name") String candidateName) {
}
