package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating application status.
 */
@Schema(description = "Application status update request")
public record StatusUpdateRequestDTO(
                @NotNull(message = "Status is required") @Schema(description = "New application status", example = "REVIEWED") ApplicationStatus status) {
}
