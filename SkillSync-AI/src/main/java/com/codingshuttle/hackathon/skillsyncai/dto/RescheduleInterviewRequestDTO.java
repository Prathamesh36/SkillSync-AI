package com.codingshuttle.hackathon.skillsyncai.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO for rescheduling an interview.
 */
public record RescheduleInterviewRequestDTO(
        @NotNull(message = "New interview date/time is required") @Future(message = "Interview date must be in the future") LocalDateTime newInterviewDateTime,

        @NotNull(message = "Duration is required") @Min(value = 15, message = "Duration must be at least 15 minutes") Integer durationMinutes,

        String meetingLink) {
}
