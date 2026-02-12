package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.InterviewScheduleStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.RealInterviewMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Scheduled interview details")
public record InterviewResponseDTO(
                @Schema(description = "Interview ID") Long id,
                @Schema(description = "Associated application ID") Long applicationId,
                @Schema(description = "Job ID") Long jobId,
                @Schema(description = "Job title") String jobTitle,
                @Schema(description = "Company name") String companyName,
                @Schema(description = "Candidate name") String candidateName,
                @Schema(description = "Candidate email") String candidateEmail,
                @Schema(description = "Recruiter name") String recruiterName,
                @Schema(description = "Recruiter email") String recruiterEmail,
                @Schema(description = "Interview date and time") LocalDateTime interviewDateTime,
                @Schema(description = "Duration in minutes") Integer durationMinutes,
                @Schema(description = "Interview mode") RealInterviewMode interviewMode,
                @Schema(description = "Meeting link") String meetingLink,
                @Schema(description = "Interview status") InterviewScheduleStatus status) {
}
