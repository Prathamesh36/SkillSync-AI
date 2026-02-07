package com.codingshuttle.hackathon.skillsyncai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for inviting a candidate to apply for a job.
 */
public record InviteCandidateRequestDTO(
        @NotNull(message = "Candidate ID is required") Long candidateId,

        @Size(max = 1000, message = "Message must not exceed 1000 characters") String message) {
}
