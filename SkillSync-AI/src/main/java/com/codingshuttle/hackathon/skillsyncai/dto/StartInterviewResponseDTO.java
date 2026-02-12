package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response after starting a mock interview session")
public record StartInterviewResponseDTO(
                @Schema(description = "Unique session identifier") UUID sessionId,
                @Schema(description = "The first interview question") String firstQuestion,
                @Schema(description = "Total questions in this session") int totalQuestions,
                @Schema(description = "Interview mode (RESUME or TOPIC)") String mode,
                @Schema(description = "Difficulty level") String difficulty) {
}
