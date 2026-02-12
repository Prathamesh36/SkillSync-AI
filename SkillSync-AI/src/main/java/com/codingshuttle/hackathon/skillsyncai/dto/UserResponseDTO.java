package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "User profile details")
public record UserResponseDTO(
        @Schema(description = "User ID") Long id,
        @Schema(description = "Email address") String email,
        @Schema(description = "Full name") String name,
        @Schema(description = "Account role") String role,
        @Schema(description = "Short bio") String bio,
        @Schema(description = "LinkedIn URL") String linkedInUrl,
        @Schema(description = "Portfolio URL") String portfolioUrl,
        @Schema(description = "Candidate-specific profile (null for recruiters)") CandidateProfileDTO candidateProfile,
        @Schema(description = "Recruiter-specific profile (null for candidates)") RecruiterProfileDTO recruiterProfile,
        @Schema(description = "Account creation timestamp") LocalDateTime createdAt) {
}
