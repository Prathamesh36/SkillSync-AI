package com.codingshuttle.hackathon.skillsyncai.dto;

/**
 * Response DTO for ending a mock interview.
 */
public record EndInterviewResponseDTO(
        double finalScore,
        String finalFeedback) {
}
