package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Job application details including AI analysis")
public record JobApplicationResponseDTO(
        @Schema(description = "Application ID") Long id,
        @Schema(description = "Job ID") Long jobId,
        @Schema(description = "Job title") String jobTitle,
        @Schema(description = "Company name") String companyName,
        @Schema(description = "Candidate user ID") Long candidateId,
        @Schema(description = "Candidate name") String candidateName,
        @Schema(description = "Candidate email") String candidateEmail,
        @Schema(description = "Resume ID used") Long resumeId,
        @Schema(description = "Current application status") ApplicationStatus status,
        @Schema(description = "AI-generated analysis of candidate fit") String aiAnalysis,
        @Schema(description = "AI fit score (0-100)") Integer aiScore,
        @Schema(description = "Application submission timestamp") LocalDateTime appliedAt,
        @Schema(description = "Last status update timestamp") LocalDateTime updatedAt,
        @Schema(description = "Recruiter user ID (job owner)") Long recruiterId) {
}
