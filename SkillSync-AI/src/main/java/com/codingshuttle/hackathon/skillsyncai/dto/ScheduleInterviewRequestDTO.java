package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.RealInterviewMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Request to schedule a real interview for a shortlisted candidate")
public record ScheduleInterviewRequestDTO(
                @NotNull @Future @Schema(description = "Interview date and time (must be in the future)", example = "2026-03-15T14:30:00") LocalDateTime interviewDateTime,
                @NotNull @Schema(description = "Duration in minutes", example = "60") Integer durationMinutes,
                @NotNull @Schema(description = "Interview mode", example = "VIDEO_CALL") RealInterviewMode interviewMode,
                @Schema(description = "Video call or meeting link", example = "https://meet.google.com/abc-xyz-123") String meetingLink,
                @Schema(description = "Additional notes for the candidate") String notes) {
}
