package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Request to reschedule an existing interview")
public record RescheduleInterviewRequestDTO(
                @NotNull @Future @Schema(description = "New interview date and time", example = "2026-03-20T10:00:00") LocalDateTime newInterviewDateTime,
                @NotNull @Schema(description = "Updated duration in minutes", example = "45") Integer durationMinutes,
                @Schema(description = "Updated meeting link", example = "https://meet.google.com/new-link-456") String meetingLink,
                @Schema(description = "Reason for rescheduling") String notes) {
}
