package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;

/**
 * Simple response DTO for invitation acceptance result.
 */
public record InvitationAcceptResponseDTO(
        Long applicationId,
        Long jobId,
        String jobTitle,
        ApplicationStatus status,
        String message) {
}
