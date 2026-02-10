package com.codingshuttle.hackathon.skillsyncai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for returning past interview session summaries.
 */
public record InterviewHistoryDTO(
        UUID sessionId,
        String mode, // RESUME_BASED or TOPIC_BASED
        List<String> topics, // null for resume-based
        String difficulty, // null for resume-based
        Double finalScore,
        int questionCount,
        LocalDateTime startedAt,
        LocalDateTime endedAt) {
}
