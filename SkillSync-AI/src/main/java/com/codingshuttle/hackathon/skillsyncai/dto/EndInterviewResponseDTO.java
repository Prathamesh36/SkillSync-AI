package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Final mock interview results")
public record EndInterviewResponseDTO(
                @Schema(description = "Overall score (0-10)") double finalScore,
                @Schema(description = "AI-generated comprehensive feedback") String feedback,
                @Schema(description = "Total questions answered") int questionsAnswered,
                @Schema(description = "Session status") String status) {
}
