package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.RealInterviewMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO for scheduling a real interview.
 */
public record ScheduleInterviewRequestDTO(
        @NotNull(message = "Interview date and time is required") @Future(message = "Interview must be scheduled in the future") LocalDateTime interviewDateTime,

        @Min(value = 15, message = "Duration must be at least 15 minutes") int durationMinutes,

        @NotNull(message = "Interview mode is required") RealInterviewMode mode,

        String meetingLink) {
}
