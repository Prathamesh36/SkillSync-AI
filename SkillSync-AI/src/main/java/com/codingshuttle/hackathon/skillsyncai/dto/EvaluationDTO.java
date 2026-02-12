package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI evaluation of a single answer")
public record EvaluationDTO(
                @Schema(description = "Score from 0 to 10") int score,
                @Schema(description = "Key strengths identified in the answer") List<String> strengths,
                @Schema(description = "Areas for improvement") List<String> weaknesses,
                @Schema(description = "Detailed feedback") String feedback) {
}
