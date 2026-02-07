package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.RealInterviewMode;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewScheduleStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for scheduled interview details.
 */
public record InterviewResponseDTO(
        Long interviewId,
        Long applicationId,
        String jobTitle,
        String companyName,
        String candidateName,
        String recruiterName,
        LocalDateTime interviewDateTime,
        int durationMinutes,
        RealInterviewMode mode,
        String meetingLink,
        InterviewScheduleStatus status) {
}
