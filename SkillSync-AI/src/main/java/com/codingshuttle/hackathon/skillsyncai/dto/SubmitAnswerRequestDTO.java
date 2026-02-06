package com.codingshuttle.hackathon.skillsyncai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for submitting an answer in a mock interview.
 */
public record SubmitAnswerRequestDTO(
        @NotBlank(message = "Answer cannot be empty") String answer) {
}
