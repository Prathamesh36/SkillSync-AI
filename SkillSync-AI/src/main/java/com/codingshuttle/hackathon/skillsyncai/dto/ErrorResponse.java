package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standard error response body")
public record ErrorResponse(
        @Schema(description = "Error timestamp") LocalDateTime timestamp,
        @Schema(description = "Error message") String message,
        @Schema(description = "Additional error details") String details) {
}
