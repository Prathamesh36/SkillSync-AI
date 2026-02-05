package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponseDTO(
                Long id,
                String email,
                String name,
                UserRole role,
                String bio,
                String linkedInUrl,
                String portfolioUrl,
                CandidateProfileDTO candidateProfile,
                RecruiterProfileDTO recruiterProfile,
                LocalDateTime createdAt) {
}
