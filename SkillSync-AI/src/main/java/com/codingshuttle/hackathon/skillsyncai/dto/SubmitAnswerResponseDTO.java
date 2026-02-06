package com.codingshuttle.hackathon.skillsyncai.dto;

/**
 * Response DTO for submitting an answer in a mock interview.
 */
public record SubmitAnswerResponseDTO(
        EvaluationDTO evaluation,
        String nextQuestion,
        boolean interviewComplete) {
}
