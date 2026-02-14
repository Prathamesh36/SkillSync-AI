package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login credentials")
public record LoginRequestDTO(
        @Schema(description = "Registered email address", example = "john@example.com")
        String email,
        @Schema(description = "Account password", example = "securePass123")
        String password) {
}
