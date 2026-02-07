package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.CancelInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InterviewResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RescheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing scheduled interviews.
 * Provides endpoints for reschedule and cancel operations.
 * 
 * Note: Schedule and view endpoints are in ApplicationController
 * to maintain logical grouping with applications.
 */
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewScheduleController {

    private final InterviewScheduleService interviewScheduleService;

    /**
     * Reschedule an existing interview.
     * 
     * Only recruiter who owns the job can reschedule.
     * Interview must be in SCHEDULED status.
     * 
     * @param interviewId ID of the interview to reschedule
     * @param request     new scheduling details
     * @return updated interview details
     */
    @PatchMapping("/{interviewId}/reschedule")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<InterviewResponseDTO> rescheduleInterview(
            Authentication authentication,
            @PathVariable Long interviewId,
            @Valid @RequestBody RescheduleInterviewRequestDTO request) {

        String recruiterEmail = authentication.getName();
        log.info("Reschedule request: interviewId={}, recruiter={}, newDateTime={}",
                interviewId, recruiterEmail, request.newInterviewDateTime());

        InterviewResponseDTO response = interviewScheduleService.rescheduleInterview(
                interviewId, request, recruiterEmail);

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an existing interview.
     * 
     * Only recruiter who owns the job can cancel.
     * Interview must be in SCHEDULED status.
     * Reverts JobApplication status to SHORTLISTED.
     * 
     * @param interviewId ID of the interview to cancel
     * @param request     contains cancellation reason
     * @return updated interview details with CANCELLED status
     */
    @PatchMapping("/{interviewId}/cancel")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<InterviewResponseDTO> cancelInterview(
            Authentication authentication,
            @PathVariable Long interviewId,
            @Valid @RequestBody CancelInterviewRequestDTO request) {

        String recruiterEmail = authentication.getName();
        log.info("Cancel request: interviewId={}, recruiter={}, reason={}",
                interviewId, recruiterEmail, request.reason());

        InterviewResponseDTO response = interviewScheduleService.cancelInterview(
                interviewId, request, recruiterEmail);

        return ResponseEntity.ok(response);
    }
}
