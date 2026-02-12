package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to invite a candidate to a job")
public record InviteCandidateRequestDTO(
                @NotNull @Schema(description = "ID of the candidate to invite", example = "42") Long candidateId,
                @Schema(description = "Personalized invitation message", example = "We were impressed by your profile and would love to discuss this opportunity!") String message) {
}
