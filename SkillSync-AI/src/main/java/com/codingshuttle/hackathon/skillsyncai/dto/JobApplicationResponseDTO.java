package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for job application details.
 */
public record JobApplicationResponseDTO(
        Long applicationId,
        Long jobId,
        String jobTitle,
        String companyName,
        Long candidateId,
        String candidateName,
        Long resumeId,
        ApplicationStatus status,
        Double matchScoreSnapshot,
        LocalDateTime appliedAt) {
}
