package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Summary of a past mock interview session")
public record InterviewHistoryDTO(
                @Schema(description = "Session ID") UUID sessionId,
                @Schema(description = "Interview mode (RESUME or TOPIC)") String mode,
                @Schema(description = "Topics covered") List<String> topics,
                @Schema(description = "Difficulty level") String difficulty,
                @Schema(description = "Overall score (0-10)") Double score,
                @Schema(description = "Number of questions answered") int questionsAnswered,
                @Schema(description = "Total questions in session") int totalQuestions,
                @Schema(description = "Session start time") LocalDateTime startedAt,
                @Schema(description = "Session end time") LocalDateTime endedAt,
                @Schema(description = "Session status") String status) {
}
