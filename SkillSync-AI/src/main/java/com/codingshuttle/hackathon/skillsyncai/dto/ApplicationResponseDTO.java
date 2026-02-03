package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponseDTO(
        Long id,
        Long userId,
        String userName,
        Long jobId,
        String jobTitle,
        ApplicationStatus status,
        Double matchScore,
        String aiAnalysis,
        LocalDateTime appliedAt) {
}
