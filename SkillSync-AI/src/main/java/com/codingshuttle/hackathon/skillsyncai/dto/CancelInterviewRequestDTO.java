package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to cancel a scheduled interview")
public record CancelInterviewRequestDTO(
                @NotBlank @Schema(description = "Reason for cancellation", example = "Position has been filled") String reason) {
}
