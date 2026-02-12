package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for applying to a job.
 */
@Schema(description = "Job application request")
public record JobApplicationRequestDTO(
                @NotNull(message = "Resume ID is required") @Schema(description = "ID of the previously uploaded resume", example = "1") Long resumeId) {
}
