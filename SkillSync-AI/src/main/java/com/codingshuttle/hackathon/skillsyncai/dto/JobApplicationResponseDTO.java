package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for job application details.
 */
public record JobApplicationResponseDTO(
                Long id,
                Long jobId,
                String jobTitle,
                String companyName,
                Long candidateId,
                String candidateName,
                Long resumeId,
                ApplicationStatus status,
                Double matchScoreSnapshot,
                LocalDateTime appliedAt,
                String candidateEmail,
                String candidateHeadline,
                java.util.List<String> candidateSkills,
                String candidateLocation,
                Integer candidateExperience,
                String aiAnalysis) {
}
