package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating application status.
 */
public record StatusUpdateRequestDTO(
        @NotNull(message = "Status is required") ApplicationStatus status) {
}
