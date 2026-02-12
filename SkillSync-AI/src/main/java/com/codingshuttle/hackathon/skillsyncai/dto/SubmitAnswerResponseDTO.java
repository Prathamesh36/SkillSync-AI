package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after submitting an answer during a mock interview")
public record SubmitAnswerResponseDTO(
                @Schema(description = "AI evaluation of the answer") EvaluationDTO evaluation,
                @Schema(description = "Next question (null if interview is over)") String nextQuestion,
                @Schema(description = "Whether the interview is complete") boolean interviewComplete,
                @Schema(description = "Current question number") int currentQuestionNumber) {
}
