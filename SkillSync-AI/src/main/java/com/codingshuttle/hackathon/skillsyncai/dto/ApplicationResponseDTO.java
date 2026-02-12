package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Application response (legacy format)")
public record ApplicationResponseDTO(
                @Schema(description = "Application ID") Long id,
                @Schema(description = "Applicant user ID") Long userId,
                @Schema(description = "Applicant name") String userName,
                @Schema(description = "Job ID") Long jobId,
                @Schema(description = "Job title") String jobTitle,
                @Schema(description = "Application status") ApplicationStatus status,
                @Schema(description = "AI match score") Double matchScore,
                @Schema(description = "AI analysis text") String aiAnalysis,
                @Schema(description = "Submission timestamp") LocalDateTime appliedAt) {
}
