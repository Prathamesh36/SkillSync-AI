package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after accepting a job invitation")
public record InvitationAcceptResponseDTO(
                @Schema(description = "Invitation ID") Long invitationId,
                @Schema(description = "Invitation status (ACCEPTED)") String status,
                @Schema(description = "Job ID") Long jobId,
                @Schema(description = "Job title") String jobTitle,
                @Schema(description = "Company name") String companyName,
                @Schema(description = "Auto-created application ID") Long applicationId,
                @Schema(description = "Application status") String applicationStatus,
                @Schema(description = "Confirmation message") String message) {
}
