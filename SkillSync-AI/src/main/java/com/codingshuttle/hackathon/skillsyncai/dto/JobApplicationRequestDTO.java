package com.codingshuttle.hackathon.skillsyncai.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for applying to a job.
 */
public record JobApplicationRequestDTO(
        @NotNull(message = "Resume ID is required") Long resumeId) {
}
