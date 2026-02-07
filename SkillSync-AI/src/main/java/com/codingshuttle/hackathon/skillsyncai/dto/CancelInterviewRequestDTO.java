package com.codingshuttle.hackathon.skillsyncai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for cancelling an interview.
 */
public record CancelInterviewRequestDTO(
        @NotBlank(message = "Cancellation reason is required") String reason) {
}
