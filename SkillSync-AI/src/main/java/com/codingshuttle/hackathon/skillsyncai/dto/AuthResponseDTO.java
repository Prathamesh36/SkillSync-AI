package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token and user details")
public record AuthResponseDTO(
        @Schema(description = "JWT Bearer token for subsequent requests", example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(description = "Authenticated user profile") UserResponseDTO user) {
}
