package com.codingshuttle.hackathon.skillsyncai.dto;

import java.util.UUID;

/**
 * Response DTO for starting a mock interview.
 */
public record StartInterviewResponseDTO(
        UUID sessionId,
        String firstQuestion) {
}
